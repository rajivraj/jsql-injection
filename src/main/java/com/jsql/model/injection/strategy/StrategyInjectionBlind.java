/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 *
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 *******************************************************************************/
package com.jsql.model.injection.strategy;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.InjectionBlind;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * Injection strategy using blind attack.
 */
public class StrategyInjectionBlind extends AbstractStrategy {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Blind injection object.
     */
    private InjectionBlind blind;
    
    public StrategyInjectionBlind(InjectionModel injectionModel) {
        super(injectionModel);
    }

    @Override
    public void checkApplicability() throws StoppedByUserSlidingException {
        
        if (this.injectionModel.getMediatorVendor().getVendor().instance().sqlTestBlindFirst() == null) {
            LOGGER.info("No Blind strategy known for "+ this.injectionModel.getMediatorVendor().getVendor());
        } else {
            LOGGER.trace(I18n.valueByKey("LOG_CHECKING_STRATEGY") +" Blind...");
            
            this.blind = new InjectionBlind(this.injectionModel);
            
            this.isApplicable = this.blind.isInjectable();
            
            if (this.isApplicable) {
                LOGGER.debug(I18n.valueByKey("LOG_VULNERABLE") +" Blind injection");
                this.allow();
                
                Request requestMessageBinary = new Request();
                requestMessageBinary.setMessage(Interaction.MESSAGE_BINARY);
                requestMessageBinary.setParameters(this.blind.getInfoMessage());
                this.injectionModel.sendToViews(requestMessageBinary);
            } else {
                this.unallow();
            }
        }
        
    }

    @Override
    public void allow(int... i) {
        this.markVulnerability(Interaction.MARK_BLIND_VULNERABLE);
    }

    @Override
    public void unallow(int... i) {
        this.markVulnerability(Interaction.MARK_BLIND_INVULNERABLE);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException {
        return this.blind.inject(
            this.injectionModel.getMediatorVendor().getVendor().instance().sqlBlind(sqlQuery, startPosition),
            stoppable
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info(I18n.valueByKey("LOG_USING_STRATEGY") +" ["+ this.getName() +"]");
        this.injectionModel.getMediatorStrategy().setStrategy(this.injectionModel.getMediatorStrategy().getBlind());
        
        Request requestMarkBlindStrategy = new Request();
        requestMarkBlindStrategy.setMessage(Interaction.MARK_BLIND_STRATEGY);
        this.injectionModel.sendToViews(requestMarkBlindStrategy);
    }
    
    @Override
    public String getPerformanceLength() {
        return "65565";
    }
    
    @Override
    public String getName() {
        return "Blind";
    }
    
}
