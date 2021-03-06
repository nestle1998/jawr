/**
 * Copyright 2013 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * The interface of CSS bundle renderer. 
 * 
 * @author Ibrahim Chaehoi
 */
public interface CssBundleLinkRenderer extends BundleRenderer {

	/**
	 * Initialize rhe Css bundle renderer
	 * @param bundler
	 *            the bundler
	 * @param useRandomParam
	 *            the flag indicating if we use the random flag
	 * @param media
	 *            the media
	 * @param alternate
	 *            the alternate flag
	 * @param displayAlternateStyles
	 *            the flag indicating if the alternate styles must be displayed
	 * @param title
	 *            the title
	 */
	public void init(ResourceBundlesHandler bundler, Boolean useRandomParam,
			String media, boolean alternate, boolean displayAlternateStyles,
			String title);

}
