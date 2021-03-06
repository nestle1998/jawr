/**
 * Copyright 2009-2014 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.taglib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.JawrLinkRenderingException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.servlet.RendererRequestUtils;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.Base64Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for image tags.
 * 
 * @author ibrahim Chaehoi
 */
public final class ImageTagUtils {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ImageTagUtils.class);

	/** The base64 key prefix */
	private static final String BASE64_KEY_PREFIX = "base64#";

	/** The data prefix */
	private static final String DATA_PREFIX = "data:";

	/**
	 * Returns the image URL generated by Jawr from a source image path
	 * 
	 * @param imgSrc
	 *            the source image path
	 * @param base64
	 *            the flag indicating if we must encode in base64
	 * @param binaryRsHandler
	 *            the binary resource handler
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the image URL generated by Jawr from a source image path
	 */
	public static String getImageUrl(String imgSrc, boolean base64,
			BinaryResourcesHandler binaryRsHandler, HttpServletRequest request,
			HttpServletResponse response) {

		String imgUrl = null;
		boolean isIE6orIE7 = RendererRequestUtils.isIE7orLess(request);

		if (!isIE6orIE7 && base64) {
			imgUrl = getBase64EncodedImage(imgSrc, binaryRsHandler, request);
		} else {
			imgUrl = getImageUrl(imgSrc, binaryRsHandler, request, response);
		}
		return imgUrl;
	}

	/**
	 * Returns the image URL generated by Jawr from a source image path
	 * 
	 * @param imgSrc
	 *            the source image path
	 * @param binaryRsHandler
	 *            the image resource handler
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the image URL generated by Jawr from a source image path
	 */
	public static String getImageUrl(String imgSrc,
			BinaryResourcesHandler binaryRsHandler, HttpServletRequest request,
			HttpServletResponse response) {

		if (binaryRsHandler == null) {
			throw new IllegalStateException(
					"You are using a Jawr image tag while the Jawr Binary servlet has not been initialized. Initialization of Jawr Image servlet either failed or never occurred.");
		}

		String contextPath = request.getContextPath();

		imgSrc = getFullImagePath(imgSrc, binaryRsHandler, request);

		String newUrl = (String) binaryRsHandler.getCacheUrl(imgSrc);

		JawrConfig jawrConfig = binaryRsHandler.getConfig();
		if (newUrl == null) {
			try {
				newUrl = CheckSumUtils.getCacheBustedUrl(imgSrc,
						binaryRsHandler.getRsReaderHandler(), jawrConfig);
				binaryRsHandler.addMapping(imgSrc, newUrl);
			} catch (IOException e) {
				LOGGER.info("Unable to create the checksum for the image '"
						+ imgSrc + "' while generating image tag.");
			} catch (ResourceNotFoundException e) {
				LOGGER.info("Unable to find the image '" + imgSrc
						+ "' while generating image tag.");
			}
		}

		if (newUrl == null) {
			newUrl = imgSrc;
		}

		String imageServletMapping = jawrConfig.getServletMapping();
		if ("".equals(imageServletMapping)) {
			if (newUrl.startsWith("/")) {
				newUrl = newUrl.substring(1);
			}
		} else {
			newUrl = PathNormalizer.joinDomainToPath(imageServletMapping,
					newUrl);
		}

		boolean sslRequest = RendererRequestUtils.isSslRequest(request);

		newUrl = RendererRequestUtils.getRenderedUrl(newUrl, jawrConfig,
				contextPath, sslRequest);

		return newUrl;
	}

	/**
	 * Returns the full image path to handle the relative path
	 * 
	 * @param imgSrc
	 *            the image source path
	 * @param binaryRsHandler
	 *            the binary resource handler
	 * @param request
	 *            the request
	 * @return the full image path
	 */
	private static String getFullImagePath(String imgSrc,
			BinaryResourcesHandler binaryRsHandler, HttpServletRequest request) {

		String contextPath = request.getContextPath();
		// relative path
		if (!binaryRsHandler.getConfig().getGeneratorRegistry()
				.isGeneratedBinaryResource(imgSrc)
				&& !imgSrc.startsWith("/")) {
			imgSrc = PathNormalizer.concatWebPath(request.getRequestURI(),
					imgSrc);
			int idx = imgSrc.indexOf(contextPath);
			if (idx > -1) {
				imgSrc = imgSrc.substring(idx + contextPath.length());
			}
		}
		return imgSrc;
	}

	/**
	 * Sames as its counterpart, only meant to be used as a JSP EL function.
	 * 
	 * @param imgSrc the image path
	 * @param pageContext the page context
	 * @return the image URL 
	 * @throws JspException if a JSPException occurs
	 */
	public static String getImageUrl(String imgSrc, PageContext pageContext) {
		BinaryResourcesHandler imgRsHandler = (BinaryResourcesHandler) pageContext
				.getServletContext().getAttribute(
						JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
		if (null == imgRsHandler)
			throw new JawrLinkRenderingException(
					"You are using a Jawr image tag while the Jawr Image servlet has not been initialized. Initialization of Jawr Image servlet either failed or never occurred.");

		HttpServletResponse response = (HttpServletResponse) pageContext
				.getResponse();

		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();

		return getImageUrl(imgSrc, imgRsHandler, request, response);

	}

	/**
	 * Returns the base64 image of the image path given in parameter
	 * 
	 * @param imgSrc
	 *            the image path
	 * @param binaryRsHandler the binary resource handler
	 * 			
	 * @param request the HTTP request
	 * @return he base64 image of the image path given in parameter
	 * @throws JawrLinkRenderingException
	 *             if an exception occurs
	 */
	public static String getBase64EncodedImage(String imgSrc,
			BinaryResourcesHandler binaryRsHandler, HttpServletRequest request) {

		String encodedResult = null;
		if (null == binaryRsHandler) {
			throw new JawrLinkRenderingException(
					"You are using a Jawr image tag while the Jawr Image servlet has not been initialized. Initialization of Jawr Image servlet either failed or never occurred.");
		}

		imgSrc = getFullImagePath(imgSrc, binaryRsHandler, request);

		encodedResult = binaryRsHandler.getCacheUrl(BASE64_KEY_PREFIX + imgSrc);
		if (encodedResult == null) {
			try {
				String fileExtension = FileNameUtils.getExtension(imgSrc);
				String fileMimeType = (String) MIMETypesSupport
						.getSupportedProperties(ImageTagUtils.class).get(
								fileExtension);

				InputStream is = binaryRsHandler.getRsReaderHandler()
						.getResourceAsStream(imgSrc);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtils.copy(is, out, true);
				byte[] data = out.toByteArray();
				encodedResult = new String(Base64Encoder.encode(data));
				encodedResult = DATA_PREFIX + fileMimeType + ";base64,"
						+ encodedResult;
				binaryRsHandler.addMapping(BASE64_KEY_PREFIX + imgSrc,
						encodedResult);
			} catch (ResourceNotFoundException e) {
				LOGGER.warn("Unable to find the image '" + imgSrc
						+ "' while generating image tag.");
			} catch (IOException e) {
				LOGGER.warn("Unable to copy the image '" + imgSrc
						+ "' while generating image tag.");
			}
		}

		return encodedResult;
	}

}
