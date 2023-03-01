package com.aaronrenner.discordnftbot.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

@Component
public class TableNameStrategy extends SpringPhysicalNamingStrategy {

    @Autowired 
    Environment env;
    
    private final String VALUE_IDENTIFIER = "nft-bot.customName";

    @Override 
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    	String identity = env.getProperty(VALUE_IDENTIFIER);
    	if(identity == null) throw new RuntimeException(String.format("No supplied \"%s\" in config!", VALUE_IDENTIFIER));
        return new Identifier(Identifier.toIdentifier(identity).getText(), name.isQuoted());
    } 
}
