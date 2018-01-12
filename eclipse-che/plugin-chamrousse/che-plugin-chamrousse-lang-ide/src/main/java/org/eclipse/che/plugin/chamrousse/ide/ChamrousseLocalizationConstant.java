/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.chamrousse.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants.
 *
 * @author Florent Benoit
 */
public interface ChamrousseLocalizationConstant extends Messages {
  @Key("chamrousse.action.create.file.title")
  String createChamrousseFileActionTitle();

  @Key("chamrousse.action.create.file.description")
  String createChamrousseFileActionDescription();
}
