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

import org.sweble.wikitext.lazy.ParserConfigInterface;

import de.fau.cs.osr.ptk.common.ParserContext;

public class LazyParserContext
        extends
            ParserContext
{
	private int stickingScopes;
	
	private ParserScopes scope;
	
	private LinkBuilder linkBuilder;
	
	// =========================================================================
	
	@Override
	public final void clear()
	{
		this.scope = null;
		this.stickingScopes = 0;
		this.linkBuilder = null;
	}
	
	@Override
	public final void init(ParserContext parent)
	{
		LazyParserContext p = (LazyParserContext) parent;
		this.stickingScopes = p.stickingScopes;
		this.scope = p.scope;
		this.linkBuilder = null;
	}
	
	// =========================================================================
	
	public final ParserScopes getScope()
	{
		return scope;
	}
	
	public final void setScope(ParserScopes scope)
	{
		this.scope = scope;
	}
	
	public final int getStickingScopes()
	{
		return stickingScopes;
	}
	
	public final void addStickingScope(ParserScopes scope)
	{
		stickingScopes |= 1 << scope.ordinal();
	}
	
	public final LinkBuilder getLinkBuilder()
	{
		return linkBuilder;
	}
	
	public final void initLinkBuilder(ParserConfigInterface parserConfig, String target)
	{
		this.linkBuilder = new LinkBuilder(parserConfig, target);
	}
	
	// =========================================================================
	
	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + stickingScopes;
		return result;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return equals((LazyParserContext) obj);
	}
	
	public final boolean equals(LazyParserContext other)
	{
		if (this == other)
			return true;
		if (scope != other.scope)
			return false;
		if (stickingScopes != other.stickingScopes)
			return false;
		return true;
	}
}
