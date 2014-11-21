// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package org.mindswap.pellet.datatypes;

import java.util.Collections;
import java.util.Set;

import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.mindswap.pellet.utils.SetUtils;

import aterm.ATermAppl;

/**
 * @author Evren Sirin
 */
public class BaseUnionDatatype extends BaseDatatype implements UnionDatatype {
	@SuppressWarnings("deprecation")
	protected Set<Datatype> members;

	@SuppressWarnings("deprecation")
	BaseUnionDatatype(ATermAppl name, Datatype[] members) {
		super(name);

		this.members = SetUtils.create(members);
	}

	@SuppressWarnings("deprecation")
	BaseUnionDatatype(ATermAppl name, Set<Datatype> members) {
		super(name);

		this.members = members;
	}

	@SuppressWarnings("deprecation")
	BaseUnionDatatype(Datatype[] members) {
		super(null);

		this.members = SetUtils.create(members);
	}

	@SuppressWarnings("deprecation")
	BaseUnionDatatype(Set<Datatype> members) {
		super(null);

		this.members = members;
	}

	@SuppressWarnings("deprecation")
	public Set<Datatype> getMembers() {
		return Collections.unmodifiableSet(members);
	}

	@SuppressWarnings("deprecation")
	public int size() {
		int size = 0;
		for (Datatype dt : members) {
			size += dt.size();
			if (size < 0)
				return Integer.MAX_VALUE;
		}

		return size;
	}

	@SuppressWarnings("deprecation")
	public boolean contains(Object value) {
		for (Datatype dt : members) {
			if (dt.contains(value))
				return true;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean contains(Object value, AtomicDatatype datatype) {
		// Datatype valDatatype = (Datatype) datatype;
		for (Datatype dt : members) {
			if (dt instanceof AtomicDatatype) {
				if (!datatype.getPrimitiveType().equals(
						((AtomicDatatype) dt).getPrimitiveType()))
					continue;
			}
			if (dt.contains(value, datatype))
				return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public Object getValue(String value, String datatypeURI) {
		Object obj = null;
		for (Datatype dt : members) {
			obj = dt.getValue(value, datatypeURI);
			if (obj != null)
				break;
		}

		return obj;
	}

	@SuppressWarnings("deprecation")
	public Datatype singleton(Object value) {
		Datatype datatype = null;
		for (Datatype dt : members) {
			if (dt.contains(value)) {
				datatype = dt.singleton(value);
				if (datatype != null)
					break;
			}
		}

		return datatype;
	}

	@SuppressWarnings("deprecation")
	public ATermAppl getValue(int n) {
		for (Datatype dt : members) {
			int dtSize = dt.size();
			if (dtSize == ValueSpace.INFINITE || n < dtSize)
				return dt.getValue(n);
			else
				n -= dt.size();
		}

		throw new InternalReasonerException("No values for this datatype");
	}

	public String toString() {
		return "UnionDatatype " + members;
	}
}
