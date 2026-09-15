package com.kitchenpulse.order.inventory;

import com.kitchenpulse.order.common.ApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InventoryErrorDecoder implements ErrorDecoder {

	private final ErrorDecoder defaultDecoder = new Default();

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.status() == 409) {
			return new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
		}
		return defaultDecoder.decode(methodKey, response);
	}
}
