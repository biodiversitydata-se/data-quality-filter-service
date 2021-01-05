<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Admin Functions | ${grailsApplication.config.skin.orgNameLong}</title>
    <asset:javascript src="jquery.tablednd.js" />
    <asset:javascript src="jquery.js" />
    <asset:javascript src="bootbox/bootbox.min.js" />
    <asset:stylesheet src="admin.css" />
    <style>
    .smallpadding {
        padding-left: 5px;
        padding-right: 5px;
    }
    </style>
</head>
<body>
<div class="row">
    <div class="col-md-12">
        <div id="breadcrumb">
            <ol class="breadcrumb">
                <li class="active"><a href="${g.createLink(uri:"/")}">Home</a> <span class=" icon icon-arrow-right"></span></li>
            </ol>
        </div>
    </div>
</div>
%{-- escape from container-fluid --}%
<div class="container">
    <g:if test="${flash.message}">
        <div class="alert alert-warning">
            <p>${flash.message}</p>
        </div>
    </g:if>
    <g:hasErrors>
        <div class="alert alert-danger">
            <ul>
                <g:eachError var="error">
                    <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                </g:eachError>
            </ul>
        </div>
    </g:hasErrors>

    <g:if test="${publicProfiles != null}">
        <g:render template="profilesTableTemplate" model="[label:'Public Data Quality Profiles', type:'public', userId:userId, enableDefault:true, profiles:publicProfiles]"/>
    </g:if>

    <g:if test="${privateProfiles != null}">
        <g:render template="profilesTableTemplate" model="[label:'Private Data Quality Profiles', type:'private', userId:userId, enableDefault:false, profiles:privateProfiles]"/>
    </g:if>

    <div id="save-profile-modal" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><alatag:message code="profile.modal.title" default="New Profile" /></h4>
                </div>
                <div class="modal-body">
                    <g:form name="save-profile-form" action="saveProfile" useToken="true">
                        <g:hiddenField name="id" value="" />
                        <g:hiddenField name="enabled" value="false" />
                        <g:hiddenField name="isDefault" value="false" />
                        <g:hiddenField name="isPublicProfile"/>
                        <div class="form-group">
                            <label for="name"><alatag:message code="profile.modal.name.label" default="Name" /></label>
                            <input type="text" class="form-control" id="name" name="name" placeholder="Name">
                        </div>
                        <div class="form-group">
                            <label for="shortName"><alatag:message code="profile.modal.shortName.label" default="Short name" /></label>
                            <input type="text" class="form-control" id="shortName" name="shortName" placeholder="short-name">
                            <p class="help-block"><alatag:message code="profile.modal.shortName.help" default="Label used for selecting this profile in URLs and the like.  A single lower case word is preferred." /></p>
                        </div>
                        <div class="form-group">
                            <label for="description"><alatag:message code="profile.modal.description.label" default="Description" /></label>
                            <input type="text" class="form-control" id="description" name="description" placeholder="description...">
                        </div>
                        <div class="form-group">
                            <label for="contactName"><alatag:message code="profile.modal.contactName.label" default="Contact Name" /></label>
                            <input type="text" class="form-control" id="contactName" name="contactName" placeholder="Contact Name">
                        </div>
                        <div class="form-group">
                            <label for="contactEmail"><alatag:message code="profile.modal.contactName.label" default="Contact Email (Optional)" /></label>
                            <input type="email" class="form-control" id="contactEmail" name="contactEmail" placeholder="contact.email@example.org">
                        </div>
                    </g:form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="submit" form="save-profile-form" class="btn btn-primary">Save changes</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="import-profile-modal" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><alatag:message code="dq.admin.uploadprofile.modal.title" default="Import a profile" /></h4>
                </div>
                <div class="modal-body">
                    <g:form name="import-profile-form" action="importProfile" enctype="multipart/form-data" useToken="true">
                        <g:hiddenField name="isPublicProfile"/>
                        <input type="file" name="filejson"/>
                    </g:form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" form="import-profile-form" class="btn btn-primary">Upload</button>
                </div>
            </div>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(function() {
        $(document).ready(function() {
            $("#profiletable").tableDnD({
                onDragStart: function(table, row) {
                    $("#profiletable").removeClass('table-striped table-hover'); // remove row coloring so dragClass can be applied
                },
                onDragStop : handlestop,
                onDragClass: 'bg-info', // set color to the row being dragged
                dragHandle: ".dragHandle"
            });
        });

        // re-calculate display orders for profiles
        function handlestop() {
            var profilesids = [];
            var orig_displayorders = [];
            // get profile ids (top down order) and old orders
            $("#profiletable").children("tbody").children("tr").each(function( index ) {
                profilesids.push($(this).children('td').first().text().trim());
                orig_displayorders.push(parseInt($(this).attr('data-curdisplayorder')));
            });

            var new_displayorders = orig_displayorders.slice();
            new_displayorders.sort(function(a, b) {
              return a - b;
            })

            for (var i = 0; i < profilesids.length; i++) {
                // update profile if display order changed
                if (orig_displayorders[i] !== new_displayorders[i]) {
                    var form = $("#profile-" + profilesids[i]).find('form[class=updateProfileDisplayOrder]');
                    var formData = $(form).serializeArray();
                    formData.push({'name':'displayOrder', 'value': new_displayorders[i]});

                    $.ajax({
                        type: "POST",
                        url: "${g.createLink(controller: 'adminDataQuality', action: 'saveProfile')}",
                        data: formData,
                        dataType: 'json',
                        accepts: {
                            text: 'text/plain'
                        }
                    }).done(function (data) {
                        if (data) {
                            $("#profiletable").find("#profile-" + data.profile.id).attr("data-curdisplayorder", data.profile.displayOrder);
                            // update token so after each request the form has a new token
                            var form = $("#profile-" + data.profile.id).find('form[class=updateProfileDisplayOrder]');
                            $(form).find('input[name=SYNCHRONIZER_TOKEN]').val(data.token);
                        }
                    });
                }
            }

            $("#profiletable").addClass('table-striped table-hover');
        }

        $("#profiletable tr").hover(function() {
           $(this.cells[0]).find('.dragHandle').css( {'cursor':'move'});
        });

        $('input[name=enabled]').on('click', function(e) {
          $(this).closest('form').submit();
        });

        // confirm delete a profile with categories
        $('form[data-confirmation=true]').on('submit', function(e) {
            var $this = $(this);
            if (!confirm("This profile has categories defined.  Are you sure you want to delete it?")) { // TODO bootbox
                e.preventDefault();
                return false;
            }
        });

        $('.addProfileLink, .importProfileLink').click(function() {
            var isPublicControl = null;
            var className = $(this).attr('class');

            if (className.indexOf('addProfileLink') !== -1) {
                isPublicControl = $('#save-profile-modal form').find('input[name=isPublicProfile]');
            } else if (className.indexOf('importProfileLink') !== -1) {
                isPublicControl = $('#import-profile-modal form').find('input[name=isPublicProfile]');
            }

            $(isPublicControl).val($(this).attr('data-isPublicProfile'))
        })

        $('.btn-edit-profile').on('click', function(e) {
            var $this = $(this);
            var id = $this.data('id');
            var name = $this.data('name');
            var shortName = $this.data('short-name');
            var description = $this.data('description');
            var contactName = $this.data('contact-name');
            var contactEmail  = $this.data('contact-email');
            var enabled = $this.data('enabled');
            var isDefault = $this.data('is-default');
            var isPublicProfile = $this.data('isPublicProfile');

            var $saveProfileModal = $('#save-profile-modal');

            var $id = $saveProfileModal.find('input[name=id]');
            var $name = $saveProfileModal.find('input[name=name]');
            var $shortName = $saveProfileModal.find('input[name=shortName]');
            var $description = $saveProfileModal.find('input[name=description]');
            var $contactName = $saveProfileModal.find('input[name=contactName]');
            var $contactEmail = $saveProfileModal.find('input[name=contactEmail]');
            var $enabled = $saveProfileModal.find('input[name=enabled]');
            var $isDefault = $saveProfileModal.find('input[name=isDefault]');
            var $isPublicProfile = $saveProfileModal.find('input[name=isPublicProfile]');

            var oldId = $id.val();
            var oldName = $name.val();
            var oldShortName = $shortName.val();
            var oldDescription = $description.val();
            var oldContactName = $contactName.val();
            var oldContactEmail = $contactEmail.val();
            var oldEnabled = $enabled.val();
            var oldIsDefault = $isDefault.val();

            $id.val(id);
            $name.val(name);
            $shortName.val(shortName);
            $description.val(description);
            $contactName.val(contactName);
            $contactEmail.val(contactEmail);
            $enabled.val(enabled);
            $isDefault.val(isDefault);
            $isPublicProfile.val(isPublicProfile);

            var clearFormFn = function() {
                $id.val(oldId);
                $name.val(oldName);
                $shortName.val(oldShortName);
                $description.val(oldDescription);
                $contactName.val(oldContactName);
                $contactEmail.val(oldContactEmail);
                $enabled.val(oldEnabled);
                $isDefault.val(oldIsDefault);
                $saveProfileModal.off('hidden.bs.modal', clearFormFn);
            };

            $saveProfileModal.modal();

            $saveProfileModal.on('hidden.bs.modal', clearFormFn);
        });
    });
</asset:script>
</body>
</html>