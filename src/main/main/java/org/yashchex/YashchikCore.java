package org.yashchex;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import yashchex.api.ContractAPI;
import yashchex.api.Yashchex;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.yashchex.Status.*;

public class YashchikCore {

    public static final YashchikCore YASCCHIK = new YashchikCore();
    public final int NETWORK_ID_MAIN = 1;
    public final int NETWORK_ID_ROPSTEN = 3;
    private String infuraAccessToken = "YourInfuraAccessToken";
    private String infuraTestNetRopstenUrl = "https://ropsten.infura.io/" + infuraAccessToken;
    private String credentialPath = "C://projects/quickstart/helloworld-rs/.eth";
    private String password = java.util.UUID.randomUUID().toString();
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
            web3j = Web3j.build(new HttpService(infuraTestNetRopstenUrl));
            walletFile = WalletUtils.generateLightNewWalletFile(password, new File(credentialPath));
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

    public static void main(String[] args) throws Exception {
        YashchikCore yashchikCore = new YashchikCore();
        yashchikCore.run();
    }

    private void run() {
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

            arduinoPort.getLastUnviewStates().stream().forEach(
                    s -> {
                        if (Stream.of(GERCON_OPEN, WALL_DAMAGED, SAFE_DAMAGED).collect(Collectors.toList()).contains(s.status)) {
                            contract.state(false, false, "55.711601, 37.581640", "Коробка повреждена!!!");
                        } else if (GOOD_WAITING.equals(s) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                            latestTimeStamp[0] = System.currentTimeMillis();
                            contract.state(true, false, "55.711601, 37.581640", null);
                        } else if (OPENED.equals(s) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                            latestTimeStamp[0] = System.currentTimeMillis();
                            contract.state(true, true, "55.711601, 37.581640", null);
                        } else if (UNLOCKED.equals(s) && System.currentTimeMillis() - latestTimeStamp[0] > 30000) {
                            latestTimeStamp[0] = System.currentTimeMillis();
                            contract.state(true, false, "55.711601, 37.581640", null);
                        }
                    });
        }
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


}
