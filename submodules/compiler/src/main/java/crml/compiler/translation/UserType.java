package crml.compiler.translation;

import java.lang.String;

import crml.language.grammar.crmlParser;

public class UserType {
    final String SIUnit;
    final String userUnit;
    final Double rate;
    final Double offset;
    final Double u;
    final String parent;
    final String alias;
    final Boolean isPartial;
    final crmlParser.Type_defContext source;

    UserType(String SIUnit, String userUnit, Double rate, Double offset,
             Double u, String parent, String alias, Boolean isPartial, crmlParser.Type_defContext source) {
        this.SIUnit = SIUnit;
        this.userUnit = userUnit;
        this.rate = rate;
        this.offset = offset;
        this.u = u;
        this.parent = parent;
        this.alias = alias;
        this.isPartial = isPartial;
        this.source = source;
    }

    public Double userToSI() {
        return this.rate*this.u + this.offset;
    }

    public Double userToSI(Double input) {
        Double val = this.rate*input + this.offset;
        System.out.println(val + " = " + this.rate + "*" + input + " + " + this.offset);
        return this.rate*input + this.offset;
    }

    public Double SIToUser() {
        return (this.u - this.offset)/this.rate;
    }

    public Double SIToUser(Double input) {
        return (input - this.offset)/this.rate;
    }

    public Boolean equals(UserType other) {
        return this.SIUnit.equals(other.SIUnit) && this.userUnit.equals(other.userUnit) &&
               this.rate.equals(other.rate) && this.offset.equals(other.offset) &&
               this.parent.equals(other.parent) && this.alias.equals(other.alias) &&
               this.isPartial.equals(other.isPartial);
    }

    @Override
    public String toString() {
        return "SIUnit: " + SIUnit + ", userUnit: " + userUnit +
                ", rate: " + rate + ", offset: " + offset +
                ", u: " + u + ", parent: " + parent + ", alias: " + alias;
    }
}
