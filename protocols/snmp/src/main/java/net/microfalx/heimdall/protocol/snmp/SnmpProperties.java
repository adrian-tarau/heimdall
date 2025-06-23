package net.microfalx.heimdall.protocol.snmp;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.security.*;
import org.snmp4j.security.nonstandard.PrivAES192With3DESKeyExtension;
import org.snmp4j.security.nonstandard.PrivAES256With3DESKeyExtension;
import org.snmp4j.smi.OID;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.snmp")
@Getter
@Setter
public class SnmpProperties {

    /**
     * The UDP port on which the SNMP agent listens.
     */
    private int udpPort = 2161;

    /**
     * The TCP port on which the SNMP agent listens.
     */
    private int tcpPort = 2165;

    /**
     * Whether the SNMP simulator is enabled.
     */
    private boolean simulatorEnabled;

    /**
     * The interval at which events are simulated.
     */
    private Duration simulatorInterval = Duration.ofSeconds(10);

    private String agentComunityString = "heimdall";
    private String agentUserName = "heimdall";
    private String authenticationProtocol = "SHA";
    private String authenticationPassword = "WEJJkuJk1x21gAkfuAPL";
    private String privacyProtocol = "AES";
    private String privacyPassword = "0pSd3WFdmPnBgZER2gn9";

    public OID getPrivacyProtocolOid() {
        OID privProtocol = null;
        if ("DES".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = PrivDES.ID;
        } else if ("3DES".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = Priv3DES.ID;
        } else if ("AES".equalsIgnoreCase(privacyProtocol) || "AES128".equals(privacyProtocol)) {
            privProtocol = PrivAES128.ID;
        } else if ("AES192".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = PrivAES192.ID;
        } else if ("AES256".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = PrivAES256.ID;
        } else if ("AES192p".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = PrivAES192With3DESKeyExtension.ID;
        } else if ("AES256p".equalsIgnoreCase(privacyProtocol)) {
            privProtocol = PrivAES256With3DESKeyExtension.ID;
        }
        return privProtocol;
    }

    public OID getAuthenticationProtocolOid() {
        OID authProtocol = null;
        if ("MD5".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthMD5.ID;
        } else if ("SHA".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthSHA.ID;
        } else if ("SHA224".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthHMAC128SHA224.ID;
        } else if ("SHA256".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthHMAC192SHA256.ID;
        } else if ("SHA384".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthHMAC256SHA384.ID;
        } else if ("SHA512".equalsIgnoreCase(authenticationProtocol)) {
            authProtocol = AuthHMAC384SHA512.ID;
        }
        return authProtocol;
    }
}
