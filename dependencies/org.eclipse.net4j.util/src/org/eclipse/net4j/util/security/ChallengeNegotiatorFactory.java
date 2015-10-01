/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.net4j.util.security;

/**
 * @author Eike Stepper
 */
public class ChallengeNegotiatorFactory extends NegotiatorFactory
{
  public static final String TYPE = "challenge"; //$NON-NLS-1$

  public ChallengeNegotiatorFactory()
  {
    super(TYPE);
  }

  public ChallengeNegotiator create(String description)
  {
    return new ChallengeNegotiator();
  }
}
