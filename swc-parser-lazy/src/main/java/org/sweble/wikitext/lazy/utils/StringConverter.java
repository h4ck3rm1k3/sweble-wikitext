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

package org.sweble.wikitext.lazy.utils;

import org.sweble.wikitext.lazy.ParserConfigInterface;
import org.sweble.wikitext.lazy.preprocessor.Ignored;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;

import de.fau.cs.osr.ptk.common.VisitingException;
import de.fau.cs.osr.ptk.common.Visitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;

public class StringConverter
{
	public static final int RESOLVE_CHAR_REF = 0x0001;
	
	public static final int RESOLVE_ENTITY_REF = 0x0002;
	
	public static final int FAIL_ON_UNRESOLVED_XML_ENTITY = 0x0004;
	
	public static final int FAIL_ON_XML_COMMENTS = 0x0008;
	
	public static final int FAIL_ON_IGNORED = 0x0010;
	
	public static final int DEFAULT_OPTIONS = 0x0000;
	
	// =========================================================================
	
	public static String convert(AstNode astNode) throws StringConversionException
	{
		return convert(astNode, null, DEFAULT_OPTIONS);
	}
	
	public static String convert(AstNode astNode, ParserConfigInterface resolver, int options) throws StringConversionException
	{
		ConverterVisitor converter =
		        new ConverterVisitor(options, resolver);
		
		try
		{
			return (String) converter.go(astNode);
		}
		catch (VisitingException e)
		{
			if (e.getCause() instanceof StringConversionException)
				throw (StringConversionException) e.getCause();
			
			throw e;
		}
	}
	
	// =========================================================================
	
	protected static final class ConverterVisitor
	        extends
	        Visitor
	{
		private final StringBuilder result = new StringBuilder();
		
		private final ParserConfigInterface entityResolver;
		
		private final int options;
		
		// =====================================================================
		
		public ConverterVisitor(int options, ParserConfigInterface resolver)
		{
			this.entityResolver = resolver;
			this.options = options;
			
			if (resolver != null && !opt(RESOLVE_ENTITY_REF))
				throw new IllegalArgumentException(
				        "If a resolver instance is given the option RESOLVE_ENTITY_REF is required");
			
			if (resolver == null && opt(RESOLVE_ENTITY_REF))
				throw new IllegalArgumentException(
				        "If the option RESOLVE_ENTITY_REF is given a resolver instance is required");
		}
		
		@Override
		protected Object after(AstNode node, Object result)
		{
			return this.result.toString();
		}
		
		@Override
		public Object visitNotFound(AstNode node)
		{
			throw new VisitingException(new StringConversionException(node));
		}
		
		public void visit(NodeList n)
		{
			iterate(n);
		}
		
		public void visit(XmlCharRef n) throws StringConversionException
		{
			if (opt(RESOLVE_CHAR_REF))
			{
				result.append(Character.toChars(n.getCodePoint()));
			}
			else
			{
				if (opt(FAIL_ON_UNRESOLVED_XML_ENTITY))
					throw new StringConversionException(n);
				
				result.append("&#");
				result.append(n.getCodePoint());
				result.append(';');
			}
		}
		
		public void visit(XmlEntityRef n) throws StringConversionException
		{
			String replacement = null;
			if (opt(RESOLVE_ENTITY_REF))
				replacement = entityResolver.resolveXmlEntity(n.getName());
			
			if (replacement == null)
			{
				if (opt(FAIL_ON_UNRESOLVED_XML_ENTITY))
					throw new StringConversionException(n);
				
				result.append('&');
				result.append(n.getName());
				result.append(';');
			}
			else
			{
				result.append(replacement);
			}
		}
		
		public void visit(Text n)
		{
			result.append(n.getContent());
		}
		
		public void visit(XmlComment n) throws StringConversionException
		{
			if (opt(FAIL_ON_XML_COMMENTS))
				throw new StringConversionException(n);
		}
		
		public void visit(Ignored n) throws StringConversionException
		{
			if (opt(FAIL_ON_IGNORED))
				throw new StringConversionException(n);
		}
		
		private boolean opt(int x)
		{
			return (options & x) == x;
		}
	}
}
