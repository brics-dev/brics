package gov.nih.brics.cas.custom;

import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.Predicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.jasig.cas.ticket.registry.support.NoOpLockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.nih.brics.cas.logging.SessionLogDbUtil;

public class LoggingTicketRegistryCleaner implements RegistryCleaner {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /** Execution locking strategy. */
    @NotNull
    private LockingStrategy lock = new NoOpLockingStrategy();

    @NotNull
    private TicketRegistry ticketRegistry;
    
    @NotNull
    private JdbcTemplate jdbcTemplate;

    @NotNull
    private DataSource dataSource;
	
    /**
     * Instantiates a new Default ticket registry cleaner.
     *
     * @param centralAuthenticationService the CAS interface acting as the service layer
     * @param ticketRegistry the ticket registry
     */
    public LoggingTicketRegistryCleaner(final CentralAuthenticationService centralAuthenticationService,
                                        final TicketRegistry ticketRegistry) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.ticketRegistry = ticketRegistry;
    }
    
    @Override
    public Collection<Ticket> clean() {
        try {
            logger.info("Beginning ticket cleanup.");
            logger.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.lock.acquire()) {
                logger.info("Could not obtain lock.  Aborting cleanup.");
                return Collections.emptyList();
            }
            logger.debug("Acquired lock.  Proceeding with cleanup.");

            final Collection<Ticket> ticketsToRemove = this.centralAuthenticationService.getTickets(new Predicate() {
                @Override
                public boolean evaluate(final Object o) {
                    final Ticket ticket = (Ticket) o;
                    return ticket.isExpired();
                }
            });

            logger.info("{} expired tickets found to be removed.", ticketsToRemove.size());

            try {
                for (final Ticket ticket : ticketsToRemove) {
                    if (ticket instanceof TicketGrantingTicket) {
                    	// end session in session log database table
                    	SessionLogDbUtil.endSession(jdbcTemplate, ticket.getId());
                    	
                        logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                        this.centralAuthenticationService.destroyTicketGrantingTicket(ticket.getId());
                    } else if (ticket instanceof ServiceTicket) {
                        logger.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                        this.ticketRegistry.deleteTicket(ticket.getId());
                    } else {
                        logger.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }

            return ticketsToRemove;
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            this.lock.release();
            logger.info("Finished ticket cleanup.");
        }
    }
    
    /**
     * Method to set the datasource and generate a JdbcTemplate.
     *
     * @param dataSource the datasource to use.
     */
    public final void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * @param ticketRegistry The ticketRegistry to set.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        logger.warn("Invoking setTicketRegistry() is deprecated and has no impact.");
    }


    /**
     * @param  strategy  Ticket cleanup locking strategy.  An exclusive locking
     * strategy is preferable if not required for some ticket backing stores,
     * such as JPA, in a clustered CAS environment.  Use JPA locking strategies
     * for JPA-backed ticket registries in a clustered
     * CAS environment.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setLock(final LockingStrategy strategy) {
        this.lock = strategy;
    }

    /**
     * @deprecated As of 4.1, single signout callbacks are entirely controlled by the {@link LogoutManager}.
     * @param logUserOutOfServices whether to logger the user out of services or not.
     */
    @Deprecated
    public void setLogUserOutOfServices(final boolean logUserOutOfServices) {
        logger.warn("Invoking setLogUserOutOfServices() is deprecated and has no impact.");
    }

    /**
     * Set the logout manager.
     *
     * @param logoutManager the logout manager.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setLogoutManager(final LogoutManager logoutManager) {
        logger.warn("Invoking setLogoutManager() is deprecated and has no impact.");
    }
}
