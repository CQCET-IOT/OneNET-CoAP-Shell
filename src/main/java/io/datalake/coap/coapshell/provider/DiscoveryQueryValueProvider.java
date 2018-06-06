/*
 * Copyright 2018 the original author or authors.
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
package io.datalake.coap.coapshell.provider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

/**
 * @author Christian Tzolov
 */
@Component
public class DiscoveryQueryValueProvider extends ValueProviderSupport {

	private final static String[] DISCOVERY_QUERY_OPTIONS = new String[] { "href", "rt", "ct", "es", "obs" };

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter,
			CompletionContext completionContext, String[] hints) {

		return Arrays.stream(DISCOVERY_QUERY_OPTIONS)
				.filter(o -> o.startsWith(prefix(completionContext)))
				.map(contentType -> new CompletionProposal(contentType))
				.collect(Collectors.toList());
	}

	private String prefix(CompletionContext completionContext) {
		final String prefix = completionContext.currentWordUpToCursor();
		return (prefix != null) ? prefix : "";
	}
}
