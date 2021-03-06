package org.lastbamboo.common.sip.client;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.id.uuid.UUID;
import org.lastbamboo.common.offer.answer.Offerer;
import org.lastbamboo.common.sip.stack.message.Invite;
import org.littleshoot.mina.common.ByteBuffer;

/**
 * Interface for an individual SIP client connected to an individual SIP proxy.
 */
public interface SipClient extends Offerer
    {
    
    /**
     * Accessor for the unique instance ID for this client.
     * 
     * @return The instance ID for this client.
     */
    UUID getInstanceId();

    /**
     * Accessor the contact URI for the client.
     * 
     * @return The contact URI for the client.
     */
    URI getContactUri();

    /**
     * Accessor for the SIP URI for this client.
     * 
     * @return The SIP URI for this client.
     */
    URI getSipUri();

    /**
     * Accessor for the URI of the SIP proxy the client is connected to and
     * registered with.
     * 
     * @return The URI of the SIP proxy the client is connected to and
     * registered with.
     */
    URI getProxyUri();

    /**
     * Sends a CRLF keep-alive message, as specified in the SIP outbound
     * draft at:
     * 
     * http://www.ietf.org/internet-drafts/draft-ietf-sip-outbound-08.txt
     */
    void writeCrlfKeepAlive();

    /**
     * Sends an INVITE OK message.
     * 
     * @param invite The INVITE we're responding to.
     * @param body The body of the INVITE OK.
     */
    void writeInviteOk(Invite invite, ByteBuffer body);
    
    /**
     * Writes an INVITE rejected response.
     * 
     * @param invite The invite.
     * @param responseCode The response code.
     * @param reasonPhrase The reason phrase for the error.
     */
    void writeInviteRejected(Invite invite, int responseCode, 
        String reasonPhrase); 

    /**
     * Registers the SIP client.
     * @throws IOException If we do not get a successful registration response.
     */
    void register() throws IOException;

    /**
     * Connects to the proxy server.
     * 
     * @throws IOException If we cannot connect.
     */
    void connect() throws IOException;

    }
