package com.codemuni.core.keyStoresProvider;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.ocsp.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.Security;
import java.security.cert.X509Certificate;

public class OCSPUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Extracts the OCSP URI from Authority Information Access extension.
     */
    public static URI getOcspUri(X509Certificate certificate) {
        try {
            byte[] aiaExtensionValue = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
            if (aiaExtensionValue == null) return null;

            ASN1InputStream asn1InputStream1 = new ASN1InputStream(aiaExtensionValue);
            ASN1Primitive derObj1 = asn1InputStream1.readObject();
            asn1InputStream1.close();

            byte[] octets = ((ASN1OctetString) derObj1).getOctets();
            ASN1InputStream asn1InputStream2 = new ASN1InputStream(octets);
            ASN1Primitive derObj2 = asn1InputStream2.readObject();
            asn1InputStream2.close();

            AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(derObj2);
            AccessDescription[] descriptions = aia.getAccessDescriptions();

            for (AccessDescription description : descriptions) {
                if (description.getAccessMethod().equals(AccessDescription.id_ad_ocsp)) {
                    GeneralName accessLocation = description.getAccessLocation();
                    if (accessLocation.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        ASN1String uriStr = (ASN1String) accessLocation.getName();
                        return new URI(uriStr.getString());
                    }
                }
            }
        } catch (Exception e) {
            // Handle/log exception if needed
        }
        return null;
    }

    /**
     * Sends OCSP request and checks if the certificate is revoked.
     */
    public static boolean checkOcspStatus(X509Certificate cert, X509Certificate issuerCert, URI ocspUri) {
        try {
            CertificateID id = new CertificateID(CertificateID.HASH_SHA1, issuerCert, cert.getSerialNumber());

            OCSPReqGenerator gen = new OCSPReqGenerator();
            gen.addRequest(id);
            OCSPReq req = gen.generate();

            byte[] ocspReqData = req.getEncoded();
            HttpURLConnection con = (HttpURLConnection) ocspUri.toURL().openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/ocsp-request");
            con.setRequestProperty("Accept", "application/ocsp-response");
            con.setDoOutput(true);
            con.getOutputStream().write(ocspReqData);

            InputStream in = con.getInputStream();
            OCSPResp resp = new OCSPResp(in);
            in.close();

            if (resp.getStatus() == 0) { // 0 = SUCCESSFUL
                BasicOCSPResp basicResp = (BasicOCSPResp) resp.getResponseObject();
                SingleResp[] responses = basicResp.getResponses();
                for (SingleResp sr : responses) {
                    Object status = sr.getCertStatus();
                    if (status instanceof RevokedStatus) {
                        return true; // Certificate is revoked
                    }
                }
            }
        } catch (Exception e) {
            // Handle/log exception if needed
        }
        return false; // Not revoked or OCSP failed
    }
}
