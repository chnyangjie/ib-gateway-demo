package ren.wizard.gateway.demo;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("init");
        IBClient ibClient = new IBClient();
        log.info("try connect");
        ibClient.connect("127.0.0.1", 14444, 3);
        log.info("try connected");

        EClientSocket client = ibClient.getClient();
        Contract contract = new Contract();
        contract.symbol("AAPL");
        contract.currency("USD");
        contract.exchange("SMART");
        contract.secType("STK");


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String formatted = form.format(cal.getTime());
        System.out.println(formatted);
        boolean subscribe = true;
        while (true) {
            try {
                if (client.isConnected() && subscribe) {
//                    client.reqMarketDataType(3);
                    ibClient.reqMarket(contract, "233,236");
                    ibClient.reqHistoricalData(contract, cal.getTimeInMillis(), "1 M", "1 day", "MIDPOINT");
                    ibClient.search("700", false);
                    ibClient.search("AAPL", "USD", false);
                    ibClient.search("BABA", "USD", false);
                    ibClient.search("TSLA", "USD", false);
                    ibClient.search("600100", "CNH", false);
                    ibClient.search("600100", "CNY", false);
                    subscribe = false;
                }
                Thread.sleep(2000);
                log.info("is connected {}", client.isConnected());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
