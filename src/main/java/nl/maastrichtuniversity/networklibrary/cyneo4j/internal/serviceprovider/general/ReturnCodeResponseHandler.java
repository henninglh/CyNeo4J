package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.general;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

public class ReturnCodeResponseHandler implements ResponseHandler<Boolean> {

	@Override
	public Boolean handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {

		int responseCode = response.getStatusLine().getStatusCode();
		return (responseCode >= 200 && responseCode < 300);
	}

}
