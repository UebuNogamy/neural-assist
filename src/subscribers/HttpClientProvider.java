package subscribers;


import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import services.StreamJavaHttpClient;

@Creatable
@Singleton
public class HttpClientProvider
{
    @Inject
    private Provider<StreamJavaHttpClient> clientProvider;
    @Inject
    private AppendMessageToViewSubscriber appendMessageToViewSubscriber;
    @Inject
    private FunctionCallSubscriber functionCallSubscriber;
    @Inject
    private PrintMessageSubscriber printMessageSubscriber;
    
    public StreamJavaHttpClient get()
    {
        StreamJavaHttpClient client = clientProvider.get();
        client.subscribe(printMessageSubscriber);
        client.subscribe(appendMessageToViewSubscriber);
        client.subscribe(functionCallSubscriber);
        return client;
    }
}
