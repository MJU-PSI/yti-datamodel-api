package fi.vm.yti.datamodel.api.security;

import fi.vm.yti.datamodel.api.model.AbstractClass;
import fi.vm.yti.datamodel.api.model.AbstractModel;
import fi.vm.yti.datamodel.api.model.AbstractPredicate;
import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.YtiUser;

import org.apache.jena.iri.IRI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

import static fi.vm.yti.security.Role.ADMIN;
import static fi.vm.yti.security.Role.DATA_MODEL_EDITOR;

@Service
@Profile("!junit")
public class AuthorizationManagerImpl implements AuthorizationManager {

    private final AuthenticatedUserProvider userProvider;

    AuthorizationManagerImpl(AuthenticatedUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public boolean hasRightToEdit(AbstractModel model) {
        return hasRightToAnyOrganization(model.getEditorOrganizations());
    }

    public boolean hasRightToEdit(AbstractClass model) {
        return hasRightToAnyOrganization(model.getEditorOrganizations());
    }

    public boolean hasRightToEdit(AbstractPredicate model) {
        return hasRightToAnyOrganization(model.getEditorOrganizations());
    }

    public boolean hasRightToCreateNewVersion(AbstractModel model) {
        return hasRightToAnyOrganization(model.getEditorOrganizations());
       // return isAdminOfAnyOrganization(model.getOrganizations());
    }

    public boolean hasRightToAnyOrganization(Collection<UUID> organizations) {
        YtiUser user = getUser();
        return user.isSuperuser() || user.isInAnyRole(EnumSet.of(ADMIN, DATA_MODEL_EDITOR), organizations);
    }

    public boolean isAdminOfAnyOrganization(Collection<UUID> organizations) {
        YtiUser user = getUser();
        return user.isSuperuser() || user.isInAnyRole(EnumSet.of(ADMIN), organizations);
    }

    private YtiUser getUser() {
        return this.userProvider.getUser();
    }

    public boolean hasRightToRemoveClassReference(IRI modelIRI,
                                                  IRI classIRI) {
        // TODO
        return true;
    }

    public boolean hasRightToAddClassReference(IRI modelIRI,
                                               String classId) {
        // TODO
        return true;
    }

    public boolean hasRightToAddPredicateReference(IRI modelIRI,
                                                   String predicateId) {
        // TODO
        return true;
    }

    public boolean hasRightToRemovePredicateReference(IRI modelIRI,
                                                      IRI predicateIRI) {
        // TODO
        return true;
    }

    public boolean canReplicate() {
        return this.getUser().isSuperuser();
    }

    public boolean hasRightToDoMigration() {
        return getUser().isSuperuser();
    }

    public boolean hasRightToRunSparqlQuery() {
        return getUser().isSuperuser();
    }

    public boolean hasRightToSuggestConcept() {
        YtiUser user = getUser();
        return user.isSuperuser() || !user.getOrganizations(ADMIN, DATA_MODEL_EDITOR).isEmpty();
    }

    public boolean hasRightToDropDatabase() {
        return getUser().isSuperuser();
    }
}
