/*
   Copyright 2021 mark.nellemann@gmail.com

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

import biz.nellemann.syslogd.msg.SyslogMessage;
import java.util.EventObject;

public class LogForwardEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private final SyslogMessage message;

    public LogForwardEvent(final Object source, final SyslogMessage message ) {
        super( source );
        this.message = message;
    }

    public SyslogMessage getMessage() {
        return message;
    }

}
