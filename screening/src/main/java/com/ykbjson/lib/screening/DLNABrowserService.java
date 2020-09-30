package com.ykbjson.lib.screening;

import com.ykbjson.lib.screening.xml.DLNAUDA10ServiceDescriptorBinderSAXImpl;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;


/**
 * Description：DLNABrowserService
 * <BR/>
 * Creator：yankebin
 * <BR/>
 * CreatedAt：2019-07-10
 */
public class DLNABrowserService extends AndroidUpnpServiceImpl {
    //返回DLNAUDA10ServiceDescriptorBinderSAXImpl以替代cling自带的无法在
    //Android9.0上面正常工作的UDA10ServiceDescriptorBinderSAXImpl
    //所以 在使用这个库的时候 在app module 的manifes里声明的是DLNABrowserService
    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {
            @Override
            public ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
                return new DLNAUDA10ServiceDescriptorBinderSAXImpl();
            }
        };
    }
}
