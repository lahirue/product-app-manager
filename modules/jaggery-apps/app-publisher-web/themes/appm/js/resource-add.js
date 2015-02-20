var tiers ={}; //contains Throttling tier details

var throttlingTierControlBlock; //html formatted block for throttling tiers list

var RESOURCES = [
    {"url_pattern":"/*", "http_verb":"GET" ,  "throttling_tier":"", "user_roles":"" },
    {"url_pattern":"/*", "http_verb":"POST"  , "throttling_tier":"", "user_roles":"" },
    {"url_pattern":"/*", "http_verb":"PUT" ,  "throttling_tier":"", "user_roles":"" },
    {"url_pattern":"/*", "http_verb":"DELETE" , "throttling_tier":"", "user_roles":"" },
    {"url_pattern":"/*", "http_verb":"OPTIONS" , "throttling_tier":"", "user_roles":"" },
];


$( document ).ready(function() {


    $("#overview_context").attr('maxlength','200');

    //get Tier details from tier.xml
    $.ajax({
        url: '/publisher/api/entitlement/get/Tiers',
        type: 'GET',
        contentType: 'application/json',
        dataType: 'json',
        async: false,
        success: function (data) {
            tiers = data;
            throttlingTierControlBlock = drawThrottlingTiersDynamically();
        },
        error: function () {
        }
    });

    //fixed chrome issue with file paths
    $('input[type=file]').on('change', function(e) {
        var filename = $(e.currentTarget).val().replace(/^.*\\/, "");
        $(this).parent().parent().find('.txt-filepath').val(filename);
    });

    // get the Shared entitlement partials
    $.ajax({
        url: '/publisher/api/entitlement/get/shared/policy/partial/list',
        type: 'GET',
        contentType: 'application/json',
        dataType: 'json',
        success: function (data) {

            for (var i = 0; i < data.length; i++) {
                policyPartialsArray.push({
                    id: data[i].partialId,
                    policyPartialName: data[i].partialName,
                    policyPartial: data[i].partialContent,
                    isShared: data[i].isShared,
                    author: data[i].author
                })
            }
            updatePolicyPartial();

        },
        error: function () {
        }
    });

    //load all available java policy list from DB
    loadAvailableJavaPolicies(null);



    $("#add_resource").click(function () {

        $(".http_verb").each(function () {
            var resource = {};
            resource.url_pattern = $("#url_pattern").val();
            resource.http_verb = $(this).val();
            resource.user_roles = $("#user_roles").val();
            if ($(this).is(':checked')) {
                if (resource.url_pattern != "") {
                    RESOURCES.push(resource);
                }

            }
        })

        resetResource();
        $("#resource_tbody").trigger("draw");
    });

    /**
     * this will collect trasport radio values and pushinto hidden input filed
     */
    $(".trans_checkbox").click(function(){
        var output = [];
        $( ".trans_checkbox" ).each(function( index ) {
            var value = $(this).data('value');
            if( $(this).is(':checked')){
                output.push(value);
            }
        });
        $('#overview_transports').val(output);


    });

    /**
     * this will collect trasport radio values and pushinto hidden input filed
     */
    $(".anonymous_checkbox").click(function () {
        var output = [];
        $(".anonymous_checkbox").each(function (index) {
            if ($(this).is(':checked')) {
                output.push("TRUE");
            }
            else {
                output.push("FALSE");
            }
        });
        $('#overview_allowAnonymous').val(output);
    });



    $("#clear_resource").click(function(){
        resetResource();
    });

    $("#resource_tbody").delegate(".delete_resource","click", function(){


        var conf = confirm("Are you sure you want to delete the selected resource?");
        if (conf == true) {

            var i = $(this).attr("data-index");
            RESOURCES.splice(i, 1);

            // Invalidate relevant entitlement policy
            invalidateEntitlementPolicy(i);

            $("#resource_tbody").trigger("draw");

        }


    });

    $("#resource_tbody").on("draw", function () {
        $("#resource_tbody").html("");
        for (var i = 0; i < RESOURCES.length; i++) {
            $("#resource_tbody").prepend(
                "<tr> \
                  <td><span style='color:#999'>/{context}/{version}/</span>" + RESOURCES[i].url_pattern + " <input type='hidden' value='" + RESOURCES[i].url_pattern + "' name='uritemplate_urlPattern" + i + "'/></td> \
                  <td><strong>" + RESOURCES[i].http_verb + "</strong><input type='hidden' value='" + RESOURCES[i].http_verb + "' name='uritemplate_httpVerb" + i + "'/></td> \
                    <td style='padding:0px'><select name='uritemplate_policyGroupId" + i + "' id='uritemplate_policyGroupId" + i + "' onChange='updateDropdownPolicyGroup(" + i + ");'   class='policy_groups form-control'>" + policyGroupBlock + "</select></td>\
                     <td> \
                  	<a data-index='" + i + "' class='delete_resource'><i class='icon-remove-sign'></i>  Delete</a>&nbsp; \
                  </td> \
                </tr> \
				"
            );

            //set policy group id value
            if (RESOURCES[i].policyGroupId !== undefined && RESOURCES[i].policyGroupId !== '') {
                $('#uritemplate_policyGroupId' + i).val(RESOURCES[i].policyGroupId);
            }

            updatePolicyPartial();

        }
    });

    $("#resource_tbody").trigger("draw");

    //handle global policies checkbox logic
    $(document).on("click", '.controll_visibility', function () {
        if($(this).context.checked){
            $('#token-input-roles').show();
            $('.global_role>ul.token-input-list-facebook').css('border','1px solid #ccc');
        }else{
            $('#token-input-roles').hide();
            $('#roles').tokenInput("clear");
            $('.global_role>ul.token-input-list-facebook').css('border','none');
        }
    });

    $(document).on("click", '.controll_overview_logoutUrl', function () {
        $(this).context.checked ? $('#overview_logoutUrl').show() : $('#overview_logoutUrl').hide();
    })
    //set default on loading
    $('#token-input-roles').hide();
    $('#overview_logoutUrl').hide();
    $('.global_role>ul.token-input-list-facebook').css('border','none');

    savePolicyGroupData();
    //forced commit by team
});

function resetResource() {
    $("#url_pattern").val("");
    $(".http_verb").each(function () {
        this.checked = false;
    })
}

/**
 * Fires when user changes the policy group values
 * @param index : row id
 */
function updateDropdownPolicyGroup(index) {
    var policyGroupIdElement = document.getElementById("uritemplate_policyGroupId" + index);
    RESOURCES[index].policyGroupId = policyGroupIdElement.options[policyGroupIdElement.selectedIndex].value;
}


/**
 * set policy group id value
 */
function setPolicyGroupValue() {
         for (var i = 0; i < RESOURCES.length; i++) {
            if (RESOURCES[i].policyGroupId !== undefined && RESOURCES[i].policyGroupId !== '') {
                $('#uritemplate_policyGroupId' + i).val(RESOURCES[i].policyGroupId);
            }
        }
}

