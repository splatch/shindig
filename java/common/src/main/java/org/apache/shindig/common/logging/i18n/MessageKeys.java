/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.common.logging.i18n;

import java.util.ResourceBundle;

/**
 * Unique [key, value] pairs for i18n logging messages.
 * The key is used as message key input in the log method and its value matched the key specified in the localized resource.properties file
 */
public interface MessageKeys {
	String MESSAGES = "org.apache.shindig.common.logging.i18n.resource";

	//AuthenticationServletFilter
	String ERROR_PARSING_SECURE_TOKEN = "errorParsingSecureToken";
	//XmlUtil
	String ERROR_PARSING_XML="commonErrorParsingXML";
	String ERROR_PARSING_EXTERNAL_GENERAL_ENTITIES="errorParsingExternalGeneralEntities";
	String ERROR_PARSING_EXTERNAL_PARAMETER_ENTITIES="errorParsingExternalParameterEntities";
	String ERROR_PARSING_EXTERNAL_DTD="errorParsingExternalDTD";
	String ERROR_PARSING_SECURE_XML="errorNotUsingSecureXML";
	String REUSE_DOC_BUILDERS="reuseDocumentBuilders";
	String NOT_REUSE_DOC_BUILDERS="notReuseDocBuilders";
	//LruCacheProvier
	String LRU_CAPACITY="LRUCapacity";
	//DynamicConfigProperty
	String EVAL_EL_FAILED="evalExpressionFailed";
	//JsonContainerConfigLoader
	String READING_CONFIG="readingContainerConfig";
	String LOAD_FR_STRING="loadFromString";
	String LOAD_RESOURCES_FROM="loadResourcesFrom";
	String LOAD_FILES_FROM="loadFilesFrom";
	//ApiServlet
	String API_SERVLET_PROTOCOL_EXCEPTION="apiServletProtocolException";
	String API_SERVLET_EXCEPTION="apiServletException";
	//RegistryFeature
	String OVERRIDING_FEATURE="overridingFeature";
	//XSDValidator
	String RESOLVE_RESOURCE="resolveResource";
	String FAILED_TO_VALIDATE="failedToValidate";


	//FeatureResourceLoader
	String MISSING_FILE="missingFile";
	String UNABLE_RETRIEVE_LIB="unableRetrieveLib";
	//BasicHttpFetcher
	String TIMEOUT_EXCEPTION="timeoutException";
	String EXCEPTION_OCCURRED="exceptionOccurred";
	String SLOW_RESPONSE="slowResponse";
	//HttpResponseMetadataHelper
	String ERROR_GETTING_MD5="errorGettingMD5";
	String ERROR_PARSING_MD5="errorParsingMD5";
	//OAuthModule
	String USING_RANDOM_KEY="usingRandomKey";
	String USING_FILE="usingFile";
	String LOAD_KEY_FILE_FROM="loadKeyFileFrom";
	String COULD_NOT_LOAD_KEY_FILE="couldNotLoadKeyFile";
	String COULD_NOT_LOAD_SIGN_KEY="couldNotLoadSignedKey";
	String FAILED_TO_INIT="failedToInit";
	//OauthRequest
	String OAUTH_FETCH_FATAL_ERROR="oauthFetchFatalError";
	String BOGUS_EXPIRED="bogusExpired";
	String OAUTH_FETCH_ERROR_REPROMPT="oauthFetchErrorReprompt";
	String OAUTH_FETCH_UNEXPECTED_ERROR="oauthFetchUnexpectedError";
	String UNAUTHENTICATED_OAUTH="unauthenticatedOauth";
	String INVALID_OAUTH="invalidOauth";
	//CajaCssSanitizer
	String FAILED_TO_PARSE="failedToParse";
	String UNABLE_TO_CONVERT_SCRIPT="unableToConvertScript";
	//PipelineExecutor
	String ERROR_PRELOADING="errorPreloading";
	//Processor
	String RENDER_NON_WHITELISTED_GADGET="renderNonWhitelistedGadget";
	//CajaResponseRewriter
	String FAILED_TO_RETRIEVE="failedToRetrieve";
	String FAILED_TO_READ="failedToRead";
	//DefaultServiceFetcher
	String HTTP_ERROR_FETCHING="httpErrorFetching";
	String FAILED_TO_FETCH_SERVICE="failedToFetchService";
	String FAILED_TO_PARSE_SERVICE="failedToParseService";
	//Renderer
	String FAILED_TO_RENDER="FailedToRender";
	//RenderingGadgetRewriter
	String UNKNOWN_FEATURES="unknownFeatures";
	String UNEXPECTED_ERROR_PRELOADING="unexpectedErrorPreloading";
	//SanitizingResponseRewriter
	String REQUEST_TO_SANITIZE_WITHOUT_CONTENT="requestToSanitizeWithoutContent";
	String REQUEST_TO_SANITIZE_UNKNOW_CONTENT="requestToSanitizeUnknownContent";
	String UNABLE_SANITIZE_UNKNOWN_IMG="unableToSanitizeUnknownImg";
	String UNABLE_DETECT_IMG_TYPE="unableToDetectImgType";
	//BasicImageRewriter
	String IO_ERROR_REWRITING_IMG="ioErrorRewritingImg";
	String UNKNOWN_ERROR_REWRITING_IMG="unknownErrorRewritingImg";
	String FAILED_TO_READ_IMG="failedToReadImg";
	//CssResponseRewriter
	String CAJA_CSS_PARSE_FAILURE="cajaCssParseFailure";
	//ImageAttributeRewriter
	String UNABLE_TO_PROCESS_IMG="unableToProcessImg";
	String UNABLE_TO_READ_RESPONSE="unableToReadResponse";
	String UNABLE_TO_FETCH_IMG="unableToFetchImg";
	String UNABLE_TO_PARSE_IMG="unableToParseImg";
	//MutableContent
	String EXCEPTION_PARSING_CONTENT="exceptionParsingContent";
	//PipelineDataGadgetRewriter
	String FAILED_TO_PARSE_PRELOAD="failedToParsePreload";
	//ProxyingVisitor
	String URI_EXCEPTION_PARSING="uriExceptionParsing";
	//TemplateRewriter
	String 	MALFORMED_TEMPLATE_LIB="malformedTemplateLib";
	//CajaContentRewriter
	String CAJOLED_CACHE_CREATED="cajoledCacheCreated";
	String RETRIEVE_REFERENCE="retrieveReference";
	String UNABLE_TO_CAJOLE="unableToCajole";
	//ConcatProxyServlet
	String CONCAT_PROXY_REQUEST_FAILED="concatProxyRequestFailed";
	//GadgetRenderingServlet
	String MALFORMED_TTL_VALUE="malformedTtlValue";
	//HttpRequestHandler
	String GADGET_CREATION_ERROR="gadgetCreationError";
	//ProxyServlet
	String EMBEDED_IMG_WRONG_DOMAIN="embededImgWrongDomain";
	//RpcServlet
	String BAD_REQUEST_400="badRequest400";
	//DefaultTemplateProcessor
	String EL_FAILURE="elFailure";
	//UriUtils
	String SKIP_ILLEGAL_HEADER="skipIllegalHeader";
	//AbstractSpecFactory
	String UPDATE_SPEC_FAILURE_USE_CACHE_VERSION="updateSpecFailureUseCacheVersion";
	String UPDATE_SPEC_FAILURE_APPLY_NEG_CACHE="updateSpecFailureApplyNegCache";
	//HashLockedDomainService
	String NO_LOCKED_DOMAIN_CONFIG="noLockedDomainConfig";
	//Bootstrap
	String STARTING_CONN_MANAGER_WITH="startingConnManagerWith";
	//DefaultRequestPipeline
	String CACHED_RESPONSE="cachedResponse";
	String STALE_RESPONSE="staleResponse";

    ResourceBundle BUNDLE = ResourceBundle.getBundle(MESSAGES);

}
