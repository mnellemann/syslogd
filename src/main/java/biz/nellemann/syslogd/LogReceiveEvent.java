/*
   Copyright 2020 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.syslogd;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;

public class LogReceiveEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    //private final String message;
    private final DatagramPacket packet;

    /*
    public LogReceiveEvent(final Object source, final String message ) {
        super( source );
        this.message = message;
    }
    */

    public LogReceiveEvent(final Object source, final DatagramPacket packet) {
        super( source );
        this.packet = packet;
    }

    public String getText() {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    public byte[] getBytes() {
        return packet.getData();
    }

}
