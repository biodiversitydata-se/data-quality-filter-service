<div class="row">
    <div class="col-md-12" style="margin-bottom: 5px">
        <h1>${label}</h1>
        <g:set var="disableCreate" value="${type == 'private' && profiles != null && profiles.size() == 1}"/>

        <g:if test="${disableCreate}">
            <a class="addProfileLink btn btn-primary" role="button" title="<alatag:message code="dq.admin.add.profile.button.only_one_can_be_created" default="Only one personal profile can be created" />" disabled><alatag:message code="add.profile.button" default="Add Profile" /></a>
            <a class="importProfileLink btn btn-primary" role="button" title="<alatag:message code="dq.admin.add.profile.button.only_one_can_be_imported" default="Only one personal profile can be created, please delete your personal profile to import a profile" />" disabled><alatag:message code="dq.admin.import.profile.button" default="Import a profile"/></a>
        </g:if>
        <g:else>
            <a class="addProfileLink btn btn-primary" role="button" data-toggle="modal" data-target="#save-profile-modal" data-isPublicProfile="${type == 'public'}"><alatag:message code="add.profile.button" default="Add Profile" /></a>
            <a class="importProfileLink btn btn-primary" role="button" data-toggle="modal" data-target="#import-profile-modal" data-isPublicProfile="${type == 'public'}"><alatag:message code="dq.admin.import.profile.button" default="Import a profile"/></a>
        </g:else>

    </div>
    <div class="col-md-12">
        <table id="profiletable" class="table table-bordered table-hover table-striped table-responsive">
            <thead>
            <tr>
                <th>Id</th>
                <th>Name</th>
                <th>short-name</th>
                <th>enabled</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${profiles != null && profiles.size() > 0}">
                <g:each in="${profiles.sort{it.displayOrder}}" var="profile">
                    <tr id="profile-${profile.id}" data-curdisplayorder="${profile.displayOrder}">
                        <td style="vertical-align: middle; width:10%"><img src="${assetPath(src: 'menu.png')}" class="dragHandle" data-toggle="tooltip" title="drag/drop to re-order profiles"></img>&nbsp;&nbsp;${profile.id}</td>
                        <td style="vertical-align: middle; width:35%"><g:link action="filters" id="${profile.id}">${profile.name}</g:link></td>
                        <td style="vertical-align: middle; width:20%">${profile.shortName}</td>
                        <td style="vertical-align: middle; width:5%">
                            <g:form action="enableQualityProfile" useToken="true">
                                <g:hiddenField name="id" value="${profile.id}" />
                                <g:hiddenField name="userId" value="${profile.userId}" />
                                <g:checkBox name="enabled" value="${profile.enabled}" disabled="${profile.isDefault}"  />
                            </g:form>
                        </td>
                        <td style="width:30%">
                            <button data-id="${profile.id}" data-name="${profile.name}" data-short-name="${profile.shortName}" data-description="${profile.description}" data-contact-name="${profile.contactName}" data-contact-email="${profile.contactEmail}" data-is-default="${profile.isDefault}" data-enabled="${profile.enabled}" data-isPublicProfile="${type == 'public'}" class="btn btn-default btn-edit-profile"><i class="fa fa-edit"></i></button>
                            <g:if test="${enableDefaultButton}">
                                <g:form action="setDefaultProfile" class="form-inline" style="display:inline;" useToken="true">
                                    <g:hiddenField name="id" value="${profile.id}" />
                                    <g:hiddenField name="userId" value="${profile.userId}" />
                                    <button type="submit" class="btn btn-${profile.isDefault ? 'primary' : 'default'} ${profile.isDefault ? ' active' : ''}" aria-pressed="${profile.isDefault}">Default</button>
                                </g:form>
                            </g:if>
                            <g:form action="deleteQualityProfile" data-confirmation="${profile.categories.size() > 0}" class="form-inline" style="display:inline;" useToken="true">
                                <g:hiddenField name="id" value="${profile.id}" />
                                <g:hiddenField name="userId" value="${profile.userId}" />
                                <button type="submit" class="btn btn-danger" ${profile.isDefault ? 'disabled' : ''}><i class="fa fa-trash"></i></button>
                            </g:form>
                            <g:link action="exportProfile" params="${[profileId: profile.id]}"><button class="btn btn-default"><alatag:message code="dq.admin.export.profile.button" default="Export profile"/></button></g:link>
                            <g:form class="updateProfileDisplayOrder" useToken="true">
                                <g:hiddenField name="id" value="${profile.id}"/>
                                <g:hiddenField name="isPublicProfile" value="true"/>
                            </g:form>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            </tbody>
        </table>
    </div>
</div>