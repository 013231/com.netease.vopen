/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vopen.app;
import vopen.protocol.VopenService;
import vopen.response.UiEventTransport;
import common.pal.PalLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
           PalLog.i("BootReceiver", "Intent.ACTION_SHUTDOWN");
           UiEventTransport event = new UiEventTransport(UiEventTransport.UIEVENT_TYPE_BOOT_SHUTDOWN);
		   VopenService.getInstance().doNotifyOtherWindow(event);
        }
        
        
    }
}
