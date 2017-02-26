/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.movie.entities;

import org.tinymediamanager.core.entities.Person;

/**
 * The Class MovieProducer. This class is representing a movie producer
 * 
 * @author Manuel Laggner
 */
public class MovieProducer extends Person {
  public MovieProducer() {
    super();
  }

  public MovieProducer(String name) {
    super(name);
  }

  public MovieProducer(String name, String role) {
    super(name);
    setRole(role);
  }
}
