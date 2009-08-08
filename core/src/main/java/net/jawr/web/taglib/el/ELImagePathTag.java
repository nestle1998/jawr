/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.taglib.el;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import net.jawr.web.taglib.ImagePathTag;

/**
 * This class defines the EL version of image path tag
 * 
 * @author ibrahim Chaehoi
 *
 */
public class ELImagePathTag extends ImagePathTag {

	/** The serial version UID */
	private static final long serialVersionUID = 8105212407595262330L;
	
	/**
	 * Instance variable mapped to "src" tag attribute. (Mapping set in
	 * associated BeanInfo class.)
	 */
	private String srcExpr;

	/**
	 * Returns the srcExpr
	 * 
	 * @return the srcExpr
	 */
	public String getSrcExpr() {
		return srcExpr;
	}

	/**
	 * Sets the srcExpr
	 * 
	 * @param srcExpr
	 *            the srcExpr to set
	 */
	public void setSrcExpr(String srcExpr) {
		this.srcExpr = srcExpr;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#doStartTag()
	 */
	public int doStartTag() throws JspException {

		String string = null;

		if (srcExpr != null) {
			string = (String) ExpressionEvaluatorManager.evaluate("srcExpr",
					srcExpr, String.class, this, pageContext);
			setSrc(string);
		}

		return super.doStartTag();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	public void release() {
		super.release();
		setSrcExpr(null);
	}
}
