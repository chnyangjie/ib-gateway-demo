package ren.wizard.gateway.demo;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class IBClient {
    private final EWrapperImpl eWrapper = new EWrapperImpl();
    private final EClientSocket client = eWrapper.getClientSocket();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private final LinkedBlockingDeque<ContractSearchRequest> contractSearchQueue = new LinkedBlockingDeque<>();
    private int second = 0;
    private int sequence = 1;

    public IBClient() {
        nextId();
    }

    public EClientSocket getClient() {
        return client;
    }

    public void connect(String host, int port, int clientId) {
        client.eConnect(host, port, clientId);
        EReader reader = new EReader(client, eWrapper.getReaderSignal());
        reader.start();
        new Thread(() -> {
            while (true) {
                eWrapper.getReaderSignal().waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }
            }
        }).start();
        startConsumeSearchRequest();

    }

    public void reqMarket(Contract contract, String genericTickList) {
        client.reqMktData(nextId(), contract, genericTickList, false, false, null);
    }

    public void reqHistoricalData(Contract contract, long endTimestamp, String durationStr, String barSizeSetting, String whatToShow) {
        String formatted = this.format.format(new Date(endTimestamp));
        client.reqHistoricalData(nextId(), contract, formatted, durationStr, barSizeSetting, whatToShow, 1, 1, false, null);
    }

    public void search(String searchKey, boolean jump) throws InterruptedException {
        ContractSearchRequest searchRequest = ContractSearchRequest.builder().symbol(searchKey).build();
        if (jump) {
            contractSearchQueue.putFirst(searchRequest);
        } else {
            contractSearchQueue.put(searchRequest);
        }
    }

    public void search(String searchKey, String currency, boolean jump) throws InterruptedException {
        ContractSearchRequest searchRequest = ContractSearchRequest.builder().symbol(searchKey).currency(currency).build();
        if (jump) {
            contractSearchQueue.putFirst(searchRequest);
        } else {
            contractSearchQueue.put(searchRequest);
        }
    }

    public void search(ContractSearchRequest searchRequest, boolean jump) throws InterruptedException {
        if (jump) {
            contractSearchQueue.putFirst(searchRequest);
        } else {
            contractSearchQueue.put(searchRequest);
        }
    }

    private synchronized int nextId() {
        int thisSecond = (int) System.currentTimeMillis() / 1000;
        if (thisSecond == second) {
            sequence += 1;
        } else {
            second = thisSecond;
            sequence = 1;
        }
        return second * 10 + sequence;
    }

    private void startConsumeSearchRequest() {
        new Thread(() -> {
            while (true) {
                try {
                    ContractSearchRequest searchKey = contractSearchQueue.take();
                    if (this.client.isConnected()) {
                        this.client.reqMatchingSymbols(nextId(), searchKey.getSymbol());
                    } else {
                        search(searchKey, true);
                    }
                    Thread.sleep(1002);
                } catch (InterruptedException e) {
                    log.error("take from queue failed;;", e);
                }
            }
        }).start();
    }
}
