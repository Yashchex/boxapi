package org.yashchex;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import yashchex.api.ContractAPI;
import yashchex.api.Yashchex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YashchikCore {

    public static final YashchikCore YASCCHIK = new YashchikCore();
    public final int NETWORK_ID_MAIN = 1;
    public final int NETWORK_ID_ROPSTEN = 3;
    private String infuraAccessToken = "YourInfuraAccessToken";
    private String infuraTestNetRopstenUrl = "https://ropsten.infura.io/" + infuraAccessToken;
    private String credentialPath = "C://projects/boxapi/.eth";
    private String password;
    private Web3j web3j;
    private String walletFile;
    private Credentials credentials;
    //BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
    private String publicAddress;
    private ArduinoPort arduinoPort;
    private State curState;
    private ContractAPI contractAPI;
    private Yashchex contract;

    public YashchikCore() {
        try {
            password = "zAq1xsw2";//java.util.UUID.randomUUID().toString();

            web3j = Web3j.build(new HttpService(infuraTestNetRopstenUrl));
            walletFile = "UTC--2018-05-20T12-54-34_638627200Z--829fcd3d2f72a3f9bc95f48392cf2155b7daedcb.json";//WalletUtils.generateLightNewWalletFile(password, new File(credentialPath));
            credentials = WalletUtils.loadCredentials(password, credentialPath + "/" + walletFile);
            publicAddress = credentials.getAddress();

            System.setProperty("walletPassword", password);
            System.setProperty("walletSource", credentialPath + "/" + walletFile);

            contractAPI = new ContractAPI();
            contract = contractAPI.getContract();

            arduinoPort = new ArduinoPort();
            arduinoPort.initialize();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static YashchikCore getYASCCHIK() {
        return YASCCHIK;
    }

    public void run() {
        final long[] latestTimeStamp = {System.currentTimeMillis()};
        long latestContractTimeStamp = System.currentTimeMillis();
        boolean canBeOpen = false;
        while (true) {
            if (!canBeOpen && System.currentTimeMillis() - latestContractTimeStamp > 10000) {
                latestContractTimeStamp = System.currentTimeMillis();
                try {
                    canBeOpen = contract.ifCanBeOpened(publicAddress).send();
                    if (canBeOpen) {
                        arduinoPort.unlock();
                    }
                } catch (Exception e) {
                    canBeOpen = false;
                }
            }

            State s = arduinoPort.getLastState();

            if (s != null) {

                try {
                    if (Stream.of("GERCON_OPEN", "WALL_DAMAGED", "SAFE_DAMAGED").collect(Collectors.toList()).contains(s.getStatus())) {
                        contract.state(false, false, "55.711601, 37.581640", "Коробка повреждена!!!").send();
                    } else if ("GOOD_WAITING".equals(s.getStatus()) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                        latestTimeStamp[0] = System.currentTimeMillis();
                        contract.state(true, false, "55.711601, 37.581640", "").send();
                    } else if ("OPENED".equals(s.getStatus()) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                        latestTimeStamp[0] = System.currentTimeMillis();
                        contract.state(true, true, "55.711601, 37.581640", "").send();
                    } else if ("UNLOCKED".equals(s.getStatus()) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                        latestTimeStamp[0] = System.currentTimeMillis();
                        contract.state(true, false, "55.711601, 37.581640", "").send();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<State> latestStates() {
        return arduinoPort.getLastUnviewStates();
    }

    public State getState() {
        System.out.println("-------> arduinoPort is inited: " + (arduinoPort != null));
        return arduinoPort.getLastState();
    }


    public String getPassword() {
        return password;
    }

    public String getCredentialPath() {
        return credentialPath;
    }

    public String getWalletFile() {
        return walletFile;
    }
}
