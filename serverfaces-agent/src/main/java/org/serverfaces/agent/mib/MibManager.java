/*
 * Copyright 2012 Rafael M. Pestano.  
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.serverfaces.agent.mib;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.serverfaces.agent.event.InitMibEvent;
import org.serverfaces.agent.event.UpdateMibEvent;
import org.serverfaces.agent.exception.CouldNotRetrieveDataException;
import org.serverfaces.agent.mo.MOScalarFactory;
import org.serverfaces.agent.mo.MOTableBuilder;
import org.serverfaces.agent.qualifier.Oid;
import org.serverfaces.agent.server.ServerRetriever;
import org.serverfaces.common.model.Application;
import org.serverfaces.common.qualifier.Log;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOMutableTableModel;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

/**
 *
 * @author Rafael M. Pestano - Nov 02, 2012 07:17:18 AM
 *
 * The MIB manager is the guy who asks the current server retriever for valuable
 * information to then register/update this information as managed objects in
 * agent mib(MOGroup)
 */
@ApplicationScoped
public class MibManager implements Serializable, MOGroup {

    @Inject
    @Log
    Logger log;
    private List<ManagedObject> objects = new LinkedList<ManagedObject>();
    private DefaultMOServer moServer;
    private OctetString context;
    @Inject
    OID serverName;
    @Inject
    OID serverUptime;
    @Inject
    OID serverActiveSessions;
    @Inject
    OID serverUsedMemory;
    @Inject
    OID serverAvailableMemory;
    @Inject
    OID serverCpuTime;
    @Inject
    OID serverActiveTransactions;
    @Inject
    OID serverCommitedTransactions;
    @Inject
    OID serverRollbackTransactions;
    @Inject
    OID serverActiveThreads;
    @Inject
    OID serverTotalRequests;
    @Inject
    OID serverAddress;
    @Inject
    OID serverTotalErrors;
    @Inject
    OID serverMaxResponseTime;
    @Inject
    OID serverAvgResponseTime;
    @Inject
    OID serverLog;
    @Inject
    OID serverApplications;
    @Inject 
    OID serverCommand;
    private MOTable applicationsTable;
    
    /**
     * A server retriever is the guy who retrieves information from a server,
     * the default retriever is GlassfishRetriever you can switch the retriever
     * via CDI Alternatives see:
     * http://docs.oracle.com/javaee/6/tutorial/doc/gjsdf.html
     */
    @Inject
    ServerRetriever serverRetriever;

    
  

    /**
     * 
     * initializes mib with MOServer and context and
     * register known OIDs in the management information base
     */
    public void initMIB(@Observes InitMibEvent initMibEvent) {
            this.moServer = initMibEvent.getDefaultMOServer();
            this.context = initMibEvent.getDefaultContext();
            this.initMOs();
    }
    
     /**
     * 
     * initializes mib with MOServer and context and
     * register known OIDs in the management information base
     */
    public void initMIB(DefaultMOServer moServer,OctetString context) {
            
            this.moServer = moServer;
            this.context = context;
            this.initMOs();
    }

    /**
     * register known OIDs in the management information base
     * 
     */
    private void initMOs() {
        log.debug("Registering Mib objects...");
         try {
            this.unregisterMOs(moServer, context);
            // register MOs
            addInstance(MOScalarFactory.createReadWrite(serverName,
                    serverRetriever.getServerName()));
            addInstance(MOScalarFactory.createReadWrite(serverAddress,
                    serverRetriever.getServerAddress()));
            addInstance(MOScalarFactory.createReadWrite(serverUptime,
                    serverRetriever.getServerUpTime()));
            addInstance(MOScalarFactory.createReadWrite(serverActiveSessions,
                    serverRetriever.getServerActiveSessions()));
            addInstance(MOScalarFactory.createReadWrite(serverUsedMemory,
                    serverRetriever.getServerUsedMemory()));
            addInstance(MOScalarFactory.createReadWrite(serverAvailableMemory,
                    serverRetriever.getServerAvailableMemory()));
            addInstance(MOScalarFactory.createReadWrite(serverCpuTime,
                    serverRetriever.getServerCpuTime()));
            addInstance(MOScalarFactory.createReadWrite(serverActiveTransactions,
                    serverRetriever.getServerActiveTransactions()));
            addInstance(MOScalarFactory.createReadWrite(serverCommitedTransactions,
                    serverRetriever.getServerCommitedTransactions()));
            addInstance(MOScalarFactory.createReadWrite(serverRollbackTransactions,
                    serverRetriever.getServerRollbackTransactions()));
            addInstance(MOScalarFactory.createReadWrite(serverActiveThreads,
                    serverRetriever.getServerActiveThreads()));
            addInstance(MOScalarFactory.createReadWrite(serverTotalRequests,
                    serverRetriever.getServerTotalRequests()));
            addInstance(MOScalarFactory.createReadWrite(serverLog,
                    serverRetriever.getServerLog()));
            addInstance(MOScalarFactory.createReadWrite(serverTotalErrors,
                    serverRetriever.getServerTotalErrors()));
            addInstance(MOScalarFactory.createReadWrite(serverMaxResponseTime,
                    serverRetriever.getServerMaxResponseTime()));
            addInstance(MOScalarFactory.createReadWrite(serverAvgResponseTime,
                    serverRetriever.getServerAvgResponseTime()));
            applicationsTable = this.createApplicationTable();
            addInstance(applicationsTable);
            addInstance(MOScalarFactory.createReadWrite(serverCommand,""));
            registerCommand();
            
            this.registerMOs();
        } catch (DuplicateRegistrationException ex) {
            log.debug("Could not register MIB object due to the following error:"+ex.getMessage());
            if(log.isDebugEnabled()){
                  ex.printStackTrace();
            }
        } catch (CouldNotRetrieveDataException cne){
             log.debug("Could not register MIB object due to the following error:"+cne.getMessage());
             if(log.isDebugEnabled()){
                  cne.printStackTrace();
            }
            throw cne;
        }
        log.debug("MIB objects registered successfully");
    }
    
    @Override
    public void registerMOs(MOServer server, OctetString context)
            throws DuplicateRegistrationException {
        for (ManagedObject mo : objects) {
            server.register(mo, context);
        }
    }
    
    public void registerCommand() throws DuplicateRegistrationException{
        moServer.register(MOScalarFactory.createReadWrite(serverCommand,""), new OctetString("private"));
    }

    public DefaultMOServer getMoServer() {
        return moServer;
    }

    public void setMoServer(DefaultMOServer moServer) {
        this.moServer = moServer;
    }

    public void registerMOs() throws DuplicateRegistrationException  {
            this.registerMOs(moServer, context);
        }

    public void unregisterMOs() {
        this.unregisterMOs(moServer, context);
    }
    
    

    @Override
    public void unregisterMOs(MOServer server, OctetString context) {
        for (ManagedObject mo : objects) {
            server.unregister(mo, context);
        }
        objects = new LinkedList<ManagedObject>();
    }

    public boolean addInstance(ManagedObject mo) {
        return objects.add(mo);
    }

    public boolean removeInstance(ManagedObject mo) {
        return objects.remove(mo);
    }
    
    public boolean removeInstance(ManagedObject mo, boolean unregister) {
        if(unregister){
            moServer.unregister(mo, context);
        }
        return objects.remove(mo);
    }

    public OctetString getContext() {
        return context;
    }

    public void setContext(OctetString context) {
        this.context = context;
    }

    public void updateMIB(@Observes UpdateMibEvent updateMib) {
        this.updateMIB();
    }
    
//    public void updateApplications(){
//        MOTable tableApplication
//    }

    /**
     * updates dynamic values in management information base
     */
    public void updateMIB() {
        OctetString uptime = new OctetString();
        uptime.setValue(serverRetriever.getServerUpTime());
        this.setScalar(getServerUptime(), uptime);
        this.setScalar(getServerActiveSessions(), new Gauge32(serverRetriever.getServerActiveSessions()));
        this.setScalar(getServerUsedMemory(), new Gauge32(serverRetriever.getServerUsedMemory()));
        this.setScalar(getServerAvailableMemory(), new Gauge32(serverRetriever.getServerAvailableMemory()));
        this.setScalar(getServerCpuTime(), new Gauge32(serverRetriever.getServerCpuTime()));
        this.setScalar(getServerActiveTransactions(), new Gauge32(serverRetriever.getServerActiveTransactions()));
        this.setScalar(getServerCommitedTransactions(), new Gauge32(serverRetriever.getServerCommitedTransactions()));
        this.setScalar(getServerRollbackTransactions(), new Gauge32(serverRetriever.getServerRollbackTransactions()));
        this.setScalar(getServerActiveThreads(), new Gauge32(serverRetriever.getServerActiveThreads()));
        this.setScalar(getServerTotalRequests(), new Counter64(serverRetriever.getServerTotalRequests()));
        this.setScalar(getServerTotalErrors(), new Counter64(serverRetriever.getServerTotalErrors()));
        this.setScalar(getServerMaxResponseTime(), new Gauge32(serverRetriever.getServerMaxResponseTime()));
        this.setScalar(getServerAvgResponseTime(), new Gauge32(serverRetriever.getServerAvgResponseTime()));
        this.updateApplicationTable();
        this.executeCommand();
//        this.setScalar(serverLog.get(), new OctetString(serverRetriever.getServerLog()));
    }

    public MOScalar findScalar(OID oid) {
        if(moServer != null){
             return (MOScalar) getMoServer().getManagedObject(oid, getContext());
        }
       return null;
    }
    
 

    public void setScalar(OID oid, Variable value) {
        MOScalar scalar = findScalar(oid);
        if(scalar != null){
            scalar.setValue(value);
        }
    }

    public ServerRetriever getServerRetriever() {
        return serverRetriever;
    }

    @Produces
    public MOScalar produceMOScalar(InjectionPoint ip) {
        if (ip.getAnnotated().isAnnotationPresent(Oid.class)) {
            String oidString = ip.getAnnotated().getAnnotation(Oid.class).value();
            return (MOScalar) getMoServer().getManagedObject(new OID(oidString), getContext());
        }
        return null;
    }
    
     public OID getServerName() {
        return serverName;
    }

    public OID getServerUptime() {
        return serverUptime;
    }

    public OID getServerActiveSessions() {
        return serverActiveSessions;
    }

    public OID getServerUsedMemory() {
        return serverUsedMemory;
    }

    public OID getServerAvailableMemory() {
        return serverAvailableMemory;
    }

    public OID getServerCpuTime() {
        return serverCpuTime;
    }

    public OID getServerActiveTransactions() {
        return serverActiveTransactions;
    }

    public OID getServerCommitedTransactions() {
        return serverCommitedTransactions;
    }

    public OID getServerRollbackTransactions() {
        return serverRollbackTransactions;
    }

    public OID getServerActiveThreads() {
        return serverActiveThreads;
    }

    public OID getServerTotalRequests() {
        return serverTotalRequests;
    }

    public OID getServerAddress() {
        return serverAddress;
    }

    public OID getServerLog() {
        return serverLog;
    }
    
    public OID getServerTotalErrors() {
        return serverTotalErrors;
    }
    
    public OID getServerMaxResponseTime() {
        return serverMaxResponseTime;
    }
    
    public OID getServerAvgResponseTime() {
        return serverAvgResponseTime;
    }
    
    public OID getserverApplications() {
        return serverApplications;
    }

    public MOMutableTableModel getTableModel() {
        if(applicationsTable != null){
            return (MOMutableTableModel) applicationsTable.getModel();
        }
        return null;
    }
    
    

    private MOTable createApplicationTable() {
        MOTableBuilder builder = new MOTableBuilder(getserverApplications()).
                 addColumnType(SMIConstants.SYNTAX_OCTET_STRING,MOAccessImpl.ACCESS_READ_ONLY)
		.addColumnType(SMIConstants.SYNTAX_GAUGE32,MOAccessImpl.ACCESS_READ_WRITE)
		.addColumnType(SMIConstants.SYNTAX_COUNTER64,MOAccessImpl.ACCESS_READ_WRITE)
		.addColumnType(SMIConstants.SYNTAX_COUNTER64,MOAccessImpl.ACCESS_READ_WRITE)
		.addColumnType(SMIConstants.SYNTAX_GAUGE32,MOAccessImpl.ACCESS_READ_WRITE)
		.addColumnType(SMIConstants.SYNTAX_GAUGE32,MOAccessImpl.ACCESS_READ_WRITE);
        
        for (Application application : serverRetriever.getServerApplications()) {
             builder.addRowValue(new OctetString(application.getName()));
             builder.addRowValue(new Gauge32(application.getActiveSessions()));
             builder.addRowValue(new Counter64(application.getTotalRequests()));
             builder.addRowValue(new Counter64(application.getTotalErrors()));
             builder.addRowValue(new Gauge32(application.getMaxResponseTime()));
             builder.addRowValue(new Gauge32(application.getAvgResponseTime()));
        }
        
       
       return builder.build();
        
    }
    
    /**
     * just recreate table to update server applications
     * 
     */
    public void updateApplicationTable(){
         getTableModel().clear();
         
        for (Iterator<MOTableRow> i = this.createApplicationTable().getModel().iterator(); i.hasNext();) {
             getTableModel().addRow(i.next());
        }
       
    }

    private void executeCommand() {
        MOScalar scalar = findScalar(serverCommand);
        if(scalar != null){
            String command = scalar.getValue().toString();
            if(command != null && !"".equals(command)){
                serverRetriever.executeCommand(command);
            }
        }
    }
   
}
