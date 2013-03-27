/*
 * Copyright 2012-2013 Manuel Laggner
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
package org.tinymediamanager.core.tvshow;

import javax.persistence.Entity;
import javax.persistence.Inheritance;

import org.tinymediamanager.core.MediaEntity;

/**
 * The Class TvShow.
 * 
 * @author Manuel Laggner
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvShow extends MediaEntity {

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getFanart()
   */
  @Override
  public String getFanart() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getPoster()
   */
  @Override
  public String getPoster() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setPoster(java.lang.String)
   */
  @Override
  public void setPoster(String poster) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setFanart(java.lang.String)
   */
  @Override
  public void setFanart(String fanart) {
    // TODO Auto-generated method stub

  }

}
