/*
 * Copyright (c) 2016 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hidroh.materialistic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.customtabs.ICustomTabsCallback;
import android.support.customtabs.ICustomTabsService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CustomTabsDelegateTest {
    private CustomTabsDelegate delegate;
    private ActivityController<Activity> controller;
    private Activity activity;
    private ICustomTabsService service;

    @Before
    public void setUp() throws RemoteException {
        controller = Robolectric.buildActivity(Activity.class);
        activity = controller.create().get();
        delegate = new CustomTabsDelegate();
        service = mock(ICustomTabsService.class);
        when(service.newSession(any(ICustomTabsCallback.class))).thenReturn(true);
        IBinder binder = mock(IBinder.class);
        when(binder.queryLocalInterface(anyString())).thenReturn(service);
        ShadowApplication.getInstance()
                .setComponentNameAndServiceForBindService(mock(ComponentName.class), binder);
    }

    @Test
    public void testUnboundService() {
        assertFalse(delegate.mayLaunchUrl(Uri.parse("http://example.com"), null, null));
        assertNull(delegate.getSession());
    }

    @Test
    public void testBindService() throws RemoteException {
        // bind service should create connection
        delegate.bindCustomTabsService(activity);
        List<ServiceConnection> connections = ShadowApplication.getInstance()
                .getBoundServiceConnections();
        assertThat(connections).isNotEmpty();

        // on service connected should create session and warm up client
        verify(service).warmup(anyLong());
        assertNotNull(delegate.getSession());
        verify(service).newSession(any(ICustomTabsCallback.class));

        // may launch url should success
        when(service.mayLaunchUrl(any(ICustomTabsCallback.class),
                any(Uri.class), any(Bundle.class), anyListOf(Bundle.class))).thenReturn(true);
        assertTrue(delegate.mayLaunchUrl(Uri.parse("http://example.com"), null, null));

        // on service disconnected should clear session
        delegate.unbindCustomTabsService(activity);
        assertNull(delegate.getSession());
    }

    @After
    public void tearDown() {
        controller.destroy();
    }
}
