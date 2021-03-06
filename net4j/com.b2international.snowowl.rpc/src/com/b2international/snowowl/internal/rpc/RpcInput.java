/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.internal.rpc;

import java.io.IOException;

/**
 * 
 */
public interface RpcInput {

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	int read() throws IOException;

	/**
	 * 
	 * @param len
	 * @return an array with the actually read bytes (the array's size may be smaller than {@code len})
	 * @throws IOException
	 */
	byte[] read(int len) throws IOException;

	/**
	 * 
	 * @param n
	 * @return
	 * @throws IOException
	 */
	long skip(long n) throws IOException;

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	int available() throws IOException;

	/**
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * 
	 * @param readlimit
	 */
	void mark(int readlimit);

	/**
	 * 
	 * @throws IOException
	 */
	void reset() throws IOException;

	/**
	 * 
	 * @return
	 */
	boolean markSupported();
}