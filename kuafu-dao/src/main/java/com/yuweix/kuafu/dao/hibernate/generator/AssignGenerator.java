package com.yuweix.kuafu.dao.hibernate.generator;


import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.StandardGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.util.Properties;


/**
 * @author yuwei
 */
public class AssignGenerator implements IdentifierGenerator, StandardGenerator {
	public static final String TARGET_COLUMN = "target_column";

	private String entityName;
	private String targetCol;


	@Override
	public Object generate(SharedSessionContractImplementor session, Object obj) {
		IdentifierGenerationException ex = new IdentifierGenerationException("Identifier for entity '" + entityName
				+ "' must be manually assigned before making the entity persistent");
		Object id = session.getEntityPersister(entityName, obj).getIdentifier(obj, session);
		if (id == null) {
			throw ex;
		}
		if (id instanceof Number) {
			long l = Long.parseLong(id.toString());
			if (l <= 0) {
				throw ex;
			}
		}
		return id;
	}

	@Override
	public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry) throws MappingException {
		entityName = parameters.getProperty(ENTITY_NAME);
		targetCol = parameters.getProperty(TARGET_COLUMN);
		if (entityName == null) {
			throw new MappingException("no entity name");
		}
		if (targetCol == null) {
			throw new MappingException("no target column");
		}
	}

	public boolean allowAssignedIdentifiers() {
		return true;
	}
}
