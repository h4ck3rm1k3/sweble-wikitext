/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sweble.wikitext.lazy.parser;

import java.util.ArrayList;

import org.sweble.wikitext.lazy.AstNodeTypes;
import org.sweble.wikitext.lazy.ParserConfigInterface;
import org.sweble.wikitext.lazy.ParserConfigInterface.TargetType;

import de.fau.cs.osr.ptk.common.Warning;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;

public class LinkBuilder
{
	private String target;
	
	private String linkPage;
	
	private Url linkUrl;
	
	private LinkOptionAltText alt;
	
	private LinkTitle title;
	
	// -- format
	
	private int width;
	
	private int height;
	
	private boolean upright;
	
	private ImageHorizAlign hAlign;
	
	private ImageVertAlign vAlign;
	
	private ImageViewFormat format;
	
	private boolean border;
	
	// -- warnings picked up along the way
	
	private ArrayList<Warning> warnings;
	
	// -- internal state
	
	private TargetType targetType;
	
	// =========================================================================
	
	public LinkBuilder(ParserConfigInterface parserConfig, String target)
	{
		this.target = target;
		this.targetType = parserConfig.classifyTarget(target);
		
		this.title = null;
		this.width = -1;
		this.height = -1;
		this.upright = false;
		this.hAlign = null;
		this.vAlign = null;
		this.format = null;
		this.border = false;
		this.linkUrl = null;
		this.linkPage = null;
		this.alt = null;
	}
	
	// =========================================================================
	
	public boolean isImageTarget()
	{
		return targetType == TargetType.IMAGE;
	}
	
	public boolean isValidTarget()
	{
		return targetType != TargetType.INVALID;
	}
	
	// =========================================================================
	
	public boolean addKeyword(String keyword)
	{
		ImageViewFormat f;
		if ((f = ImageViewFormat.which(keyword)) != null)
		{
			format = (format == null) ? f : format.combine(f);
			return true;
		}
		
		ImageHorizAlign h;
		if ((h = ImageHorizAlign.which(keyword)) != null)
		{
			hAlign = h;
			return true;
		}
		
		ImageVertAlign v;
		if ((v = ImageVertAlign.which(keyword)) != null)
		{
			vAlign = v;
			return true;
		}
		
		if (keyword.equals("border"))
		{
			border = true;
			return true;
		}
		
		if (keyword.equals("upright"))
		{
			upright = true;
			return true;
		}
		
		return false;
	}
	
	public void setHeight(int height)
	{
		if (height >= 0)
			this.height = height;
	}
	
	public void setWidth(int width)
	{
		if (width >= 0)
			this.width = width;
	}
	
	// =========================================================================
	
	public void setLink(AstNode target)
	{
		if (target.isNodeType(AstNodeTypes.NT_URL))
		{
			// second occurrence wins, url beats page
			this.linkPage = null;
			this.linkUrl = (Url) target;
		}
		else
		{
			// second occurrence wins, url beats page
			if (this.linkUrl != null)
				return;
			this.linkPage = ((LinkTarget) target).getContent();
		}
	}
	
	// =========================================================================
	
	public void setAlt(LinkOptionAltText alt)
	{
		this.alt = alt;
	}
	
	public void setTitle(LinkTitle title)
	{
		this.title = title;
	}
	
	// =========================================================================
	
	public AstNode build(NodeList options, String postfix)
	{
		if (this.title == null)
			this.title = new LinkTitle();
		
		if (this.targetType == TargetType.IMAGE)
		{
			if (hAlign == null)
				hAlign = ImageHorizAlign.NONE;
			
			if (vAlign == null)
				vAlign = ImageVertAlign.MIDDLE;
			
			if (format == null)
				format = ImageViewFormat.UNRESTRAINED;
			
			if (alt == null)
				alt = new LinkOptionAltText();
			
			ImageLink result = new ImageLink(
			        target,
			        options,
			        title,
			        format,
			        border,
			        hAlign,
			        vAlign,
			        width,
			        height,
			        upright,
			        linkPage,
			        linkUrl,
			        alt);
			
			finish(result);
			return result;
		}
		else
		{
			InternalLink result = new InternalLink(
			        "",
			        target,
			        title,
			        postfix);
			
			finish(result);
			return result;
		}
	}
	
	public void addWarning(Warning warning)
	{
		if (warnings == null)
			warnings = new ArrayList<Warning>();
		warnings.add(warning);
	}
	
	public void finish(AstNode n)
	{
		if (warnings != null && !warnings.isEmpty())
			n.setAttribute("warnings", warnings);
	}
}
