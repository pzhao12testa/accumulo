/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorUtil.IteratorScope;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.util.ArgumentChecker;
import org.apache.accumulo.core.util.Pair;
import org.apache.hadoop.io.Text;

/**
 * Configure an iterator for minc, majc, and/or scan. By default, IteratorSetting will be configured for scan.
 * 
 * Every iterator has a priority, a name, a class, a set of scopes, and configuration parameters.
 * 
 * A typical use case configured for scan:
 * 
 * <pre>
 * IteratorSetting cfg = new IteratorSetting(priority, &quot;myIter&quot;, MyIterator.class);
 * MyIterator.addOption(cfg, 42);
 * scanner.addScanIterator(cfg);
 * </pre>
 */
public class IteratorSetting {
  private int priority;
  private String name;
  private String iteratorClass;
  private EnumSet<IteratorScope> scopes;
  private Map<String,String> properties;
  
  /**
   * Get layer at which this iterator applies. See {@link #setPriority(int) for how the priority is used.}
   * 
   * @return the priority of this Iterator
   */
  public int getPriority() {
    return priority;
  }
  
  /**
   * Set layer at which this iterator applies.
   * 
   * @param priority
   *          determines the order in which iterators are applied (system iterators are always applied first, then user-configured iterators, lowest priority
   *          first)
   */
  public void setPriority(int priority) {
    ArgumentChecker.strictlyPositive(priority);
    this.priority = priority;
  }
  
  /**
   * Get the iterator's name.
   * 
   * @return the name of the iterator
   */
  public String getName() {
    return name;
  }
  
  /**
   * Set the iterator's name. Must be a simple alphanumeric identifier.
   * 
   * @param name
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name);
    this.name = name;
  }
  
  /**
   * Get the name of the class that implements the iterator.
   * 
   * @return the iterator's class name
   */
  public String getIteratorClass() {
    return iteratorClass;
  }
  
  /**
   * Set the name of the class that implements the iterator. The class does not have to be present on the client, but it must be available to all tablet
   * servers.
   * 
   * @param iteratorClass
   */
  public void setIteratorClass(String iteratorClass) {
    ArgumentChecker.notNull(iteratorClass);
    this.iteratorClass = iteratorClass;
  }
  
  /**
   * Get the scopes under which this iterator will be configured.
   * 
   * @return the scopes
   */
  public EnumSet<IteratorScope> getScopes() {
    return scopes;
  }
  
  /**
   * Set the scopes under which this iterator will be configured.
   * 
   * @param scopes
   *          the scopes to set
   */
  public void setScopes(EnumSet<IteratorScope> scopes) {
    ArgumentChecker.notNull(scopes);
    if (scopes.isEmpty())
      throw new IllegalArgumentException("empty scopes");
    this.scopes = scopes;
  }
  
  /**
   * Get the configuration parameters for this iterator.
   * 
   * @return the properties
   */
  public Map<String,String> getProperties() {
    return properties;
  }
  
  /**
   * Set the configuration parameters for this iterator.
   * 
   * @param properties
   *          the properties to set
   */
  public void setProperties(Map<String,String> properties) {
    this.properties.clear();
    addOptions(properties);
  }

  /**
   * @return <tt>true</tt> if this iterator has configuration parameters.
   */
  public boolean hasProperties() {
    return !properties.isEmpty();
  }
  
  /**
   * Constructs an iterator setting configured for the scan scope with no parameters. (Parameters can be added later.)
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param name
   *          the distinguishing name for the iterator
   * @param iteratorClass
   *          the fully qualified class name for the iterator
   */
  public IteratorSetting(int priority, String name, String iteratorClass) {
    this(priority, name, iteratorClass, EnumSet.of(IteratorScope.scan), new HashMap<String,String>());
  }
  
  /**
   * Constructs an iterator setting configured for the specified scopes with the specified parameters.
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param name
   *          the distinguishing name for the iterator
   * @param iteratorClass
   *          the fully qualified class name for the iterator
   * @param scopes
   *          the scopes of the iterator
   * @param properties
   *          any properties for the iterator
   */
  public IteratorSetting(int priority, String name, String iteratorClass, EnumSet<IteratorScope> scopes, Map<String,String> properties) {
    setPriority(priority);
    setName(name);
    setIteratorClass(iteratorClass);
    setScopes(scopes);
    this.properties = new HashMap<String,String>();
    setProperties(properties);
  }
  
  /**
   * Constructs an iterator setting using the given class's SimpleName for the iterator name. The iterator setting will be configured for the scan scope with no
   * parameters.
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param iteratorClass
   *          the class for the iterator
   */
  public IteratorSetting(int priority, Class<? extends SortedKeyValueIterator<Key,Value>> iteratorClass) {
    this(priority, iteratorClass.getSimpleName(), iteratorClass.getName());
  }
  
  /**
   * Constructs an iterator setting using the given class's SimpleName for the iterator name and configured for the specified scopes with the specified
   * parameters.
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param iteratorClass
   *          the class for the iterator
   * @param scopes
   *          the scopes of the iterator
   * @param properties
   *          any properties for the iterator
   */
  public IteratorSetting(int priority, Class<? extends SortedKeyValueIterator<Key,Value>> iteratorClass, EnumSet<IteratorScope> scopes,
      Map<String,String> properties) {
    this(priority, iteratorClass.getSimpleName(), iteratorClass.getName(), scopes, properties);
  }
  
  /**
   * Constructs an iterator setting configured for the scan scope with no parameters.
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param name
   *          the distinguishing name for the iterator
   * @param iteratorClass
   *          the class for the iterator
   */
  public IteratorSetting(int priority, String name, Class<? extends SortedKeyValueIterator<Key,Value>> iteratorClass) {
    this(priority, name, iteratorClass.getName());
  }
  
  /**
   * Constructs an iterator setting configured for the specified scopes with the specified parameters.
   * 
   * @param priority
   *          the priority for the iterator @see {@link #setPriority(int)}
   * @param name
   *          the distinguishing name for the iterator
   * @param iteratorClass
   *          the class for the iterator
   * @param scopes
   *          the scopes of the iterator
   * @param properties
   *          any properties for the iterator
   */
  public IteratorSetting(int priority, String name, Class<? extends SortedKeyValueIterator<Key,Value>> iteratorClass, EnumSet<IteratorScope> scopes,
      Map<String,String> properties) {
    this(priority, name, iteratorClass.getName(), scopes, properties);
  }
  
  /**
   * Add another option to the iterator.
   * 
   * @param option
   *          the name of the option
   * @param value
   *          the value of the option
   */
  public void addOption(String option, String value) {
    ArgumentChecker.notNull(option, value);
    properties.put(option, value);
  }
  
  /**
   * Remove an option from the iterator.
   * 
   * @param option
   *          the name of the option
   * @return the value previously associated with the option, or null if no such option existed
   */
  public String removeOption(String option) {
    ArgumentChecker.notNull(option);
    return properties.remove(option);
  }
  
  /**
   * Add many options to the iterator.
   * 
   * @param propertyEntries
   *          a set of entries to add to the options
   */
  public void addOptions(Set<Entry<String,String>> propertyEntries) {
    ArgumentChecker.notNull(propertyEntries);
    for (Entry<String,String> keyValue : propertyEntries) {
      addOption(keyValue.getKey(), keyValue.getValue());
    }
  }
  
  /**
   * Add many options to the iterator.
   * 
   * @param properties
   *          a map of entries to add to the options
   */
  public void addOptions(Map<String,String> properties) {
    ArgumentChecker.notNull(properties);
    addOptions(properties.entrySet());
  }
  
  /**
   * Remove all options from the iterator.
   */
  public void clearOptions() {
    properties.clear();
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("name:");
    sb.append(name);
    sb.append(", priority:");
    sb.append(Integer.toString(priority));
    sb.append(", class:");
    sb.append(iteratorClass);
    sb.append(", scopes:");
    sb.append(scopes);
    sb.append(", properties:");
    sb.append(properties);
    return sb.toString();
  }

  /**
   * A convenience class for passing column family and column qualifiers to iterator configuration methods.
   */
  public static class Column extends Pair<Text,Text> {
    
    public Column(Text columnFamily, Text columnQualifier) {
      super(columnFamily, columnQualifier);
    }
    
    public Column(Text columnFamily) {
      super(columnFamily, null);
    }
    
    public Column(String columnFamily, String columnQualifier) {
      super(new Text(columnFamily), new Text(columnQualifier));
    }
    
    public Column(String columnFamily) {
      super(new Text(columnFamily), null);
    }
    
  }
}
