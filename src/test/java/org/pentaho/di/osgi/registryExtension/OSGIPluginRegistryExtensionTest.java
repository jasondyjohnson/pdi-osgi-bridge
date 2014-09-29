/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.osgi.registryExtension;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.karaf.KarafHost;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginType;
import org.pentaho.di.osgi.StatusGetter;
import org.pentaho.di.osgi.service.lifecycle.PluginRegistryOSGIServiceLifecycleListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIPluginRegistryExtensionTest {
  private OSGIPluginRegistryExtension cachedInstance;
  private OSGIPluginTracker tracker;
  private Log logger;
  private KarafHost karafHost;
  private StatusGetter<Boolean> kettleClientEnvironmentInitialized;

  @Before
  public void setup() {
    try {
      cachedInstance = OSGIPluginRegistryExtension.getInstance();
    } catch ( IllegalStateException e ) {
      cachedInstance = null;
    }
    OSGIPluginRegistryExtension.setInstance( null );
    OSGIPluginRegistryExtension extension = new OSGIPluginRegistryExtension();
    tracker = mock( OSGIPluginTracker.class );
    extension.setTracker( tracker );
    logger = mock( Log.class );
    extension.setLogger( logger );
    karafHost = mock( KarafHost.class );
    extension.setKarafHost( karafHost );
    kettleClientEnvironmentInitialized = mock( StatusGetter.class );
    extension.setKettleClientEnvironmentInitialized( kettleClientEnvironmentInitialized );
  }

  @After
  public void tearDown() {
    OSGIPluginRegistryExtension.setInstance( cachedInstance );
  }

  @Test
  public void testInitKettleEnvironmentNotInitialized() {
    PluginRegistry registry = mock( PluginRegistry.class );
    when( kettleClientEnvironmentInitialized.get() ).thenReturn( false );
    OSGIPluginRegistryExtension.getInstance().init( registry );
    verify( karafHost ).init();
    verifyNoMoreInteractions( tracker );
  }

  @Test
  public void testInit() {
    PluginRegistry registry = mock( PluginRegistry.class );
    when( kettleClientEnvironmentInitialized.get() ).thenReturn( true );
    OSGIPluginRegistryExtension.getInstance().init( registry );
    verify( karafHost ).init();
    verify( tracker ).registerPluginClass( PluginInterface.class );
    verify( tracker ).addPluginLifecycleListener( any( Class.class ),
      any( PluginRegistryOSGIServiceLifecycleListener.class ) );
  }

  @Test
  public void testSearchForType() {
    OSGIPluginRegistryExtension.getInstance().searchForType( OSGIPluginType.getInstance() );
    verify( tracker ).registerPluginClass( OSGIPluginType.class );
  }

  @Test
  public void testGetPluginId() {
    Object pluginClass = mock( Object.class );
    String id = "TEST_ID";
    when( tracker.getBeanPluginProperty( OSGIPluginType.class, pluginClass, "ID" ) ).thenReturn( id );
    assertEquals( id, OSGIPluginRegistryExtension.getInstance().getPluginId( OSGIPluginType.class, pluginClass ) );
  }

  @Test
  public void testGetPluginException() {
    Object pluginClass = mock( Object.class );
    String id = "TEST_ID";
    RuntimeException runtimeException = new RuntimeException( "TEST_MESSAGE" );
    when( tracker.getBeanPluginProperty( OSGIPluginType.class, pluginClass, "ID" ) ).thenThrow( runtimeException );
    assertEquals( null, OSGIPluginRegistryExtension.getInstance().getPluginId( OSGIPluginType.class, pluginClass ) );
    verify( logger ).error( runtimeException.getMessage(), runtimeException );
  }
}