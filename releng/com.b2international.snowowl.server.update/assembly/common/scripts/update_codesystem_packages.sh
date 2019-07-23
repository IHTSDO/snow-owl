#!/usr/bin/env bash
#
# Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Existing username for the Snow Owl terminology server
SNOWOWL_USER=""

# The password for the Snow Owl user specified above
SNOWOWL_USER_PASSWORD=""

# The base URL of Snow Owl to use
BASE_URL="http://localhost:8080"

# The base URL for administrative services
ADMIN_BASE_URL="/snowowl/admin"

# The base URL for SNOMED-CT related services
SNOMED_BASE_URL="/snowowl/snomed-ct/v2"

# Code systems endpoint
CODE_SYSTEMS_URL="/codesystems"

# Branches endpoint
BRANCHES_URL="/branches"

# Snow Owl's JSON format
SNOWOWL_JSON_FORMAT="application/vnd.com.b2international.snowowl+json"

# Path to Snow Owl's export script
PATH_TO_EXPORT_SCRIPT=""

# The S3 bucket to use for uploading SNAPSHOT packages
S3_BUCKET=""

# The array of code systems to update
CODE_SYSTEMS_TO_UPDATE=()

# The prefix to use in RF2 archives. Identifies the environment from where the package was retrieved.
ENVIRONMENT_PREFIX=""

usage() {
	cat <<EOF
NAME:

    Snow Owl terminology server code system update script

OPTIONS:
	-h
			Show this help
	-u
			Define a username with access to Snow Owl's REST API
	-p
			Define the password for the above user
	-b
			Snow Owl base URL, defaults to 'http://localhost:8080'
	-s
			The AWS S3 bucket to use for uploading SNAPSHOT packages
	-x
			The path to Snow Owl's export script
	-e
			Prefix to use in the name of RF2 archives. Identifies the environment from where the package was retrieved.
	-c
			The code system short name to update. Multiple values can be specified. See examples.

NOTES:

	Username, password, S3 bucket, export script path and at least one code system short name is mandatory.

	The script performs the following:
		- find code system's current working branch path
		- find code system's default module ID if there is any
		- create SNAPSHOT export of the code system using the module filter if there is any
		- upload SNAPSHOT archive to the specified S3 bucket
		- update 'previousPackage' attribute of the code systems branch metadata

	Examples:

	./update_codesystem_packages.sh -u user -p pass -s url_of_s3_bucket -x path_to_export_script -c SNOMEDCT

	OR

	./update_codesystem_packages.sh -u user -p pass -b http://my-snowowl-server:8080 -s url_of_s3_bucket -x path_to_export_script -e uat -c SNOMEDCT -c SNOMEDCT-EXTENSION

EOF
}

echo_step() {
	echo_date
	echo_date "#### $@ ####"
	echo_date
}

echo_error() {
	echo_date "ERROR: $@" >&2
}

echo_date() {
	echo -e "[$(date +"%Y-%m-%d %H:%M:%S")] $@"
}

echo_exit() {
	echo_error $@
	exit 1
}

check_if_empty() {

	if [ -z "$1" ]; then
		echo_exit "$2"
	fi

}

check_if_file_exists() {

	if [ ! -f "$1" ]; then
		echo_exit "$2"
	fi

}

rest_get() {
	CURL_OUTPUT=$(curl --fail --silent --show-error --header "Accept: ${SNOWOWL_JSON_FORMAT}" --user "$SNOWOWL_USER:$SNOWOWL_USER_PASSWORD" --write-out "\n%{http_code}" "$@")
	CURL_MESSAGE=$(echo "$CURL_OUTPUT" | head -n-1)
	CURL_HTTP_STATUS=$(echo "$CURL_OUTPUT" | tail -n1)
}

rest_put() {
	CURL_OUTPUT=$(curl -X PUT --silent --show-error --header "Content-Type: ${SNOWOWL_JSON_FORMAT}" --header "Accept: ${SNOWOWL_JSON_FORMAT}" --user "$SNOWOWL_USER:$SNOWOWL_USER_PASSWORD" --write-out "\n%{http_code}" --data $1 $2)
	CURL_MESSAGE=$(echo "$CURL_OUTPUT" | head -n-1)
	CURL_HTTP_STATUS=$(echo "$CURL_OUTPUT" | tail -n1)
}

check_variables() {

	check_if_empty "${SNOWOWL_USER}" "A valid username for Snow Owl must be specified"
	check_if_empty "${SNOWOWL_USER_PASSWORD}" "Password for the Snow Owl user must be specified"
	check_if_empty "${BASE_URL}" "Snow Owl's base URL must be specified"
	check_if_empty "${S3_BUCKET}" "An AWS S3 bucket must be specified"

	check_if_empty "${PATH_TO_EXPORT_SCRIPT}" "Path to Snow Owl's export script must be specified"

	check_if_file_exists "${PATH_TO_EXPORT_SCRIPT}" "Path to Snow Owl's export script must point to an existing file"

	if [ ${#CODE_SYSTEMS_TO_UPDATE[@]} -eq 0 ]; then
		echo_exit "At least one SNOMED CT code system short name must be specified (e.g. SNOMEDCT-US)"
	fi

}

get_code_system_branch_path() {

	CODE_SYSTEM_BRANCH_PATH=$(echo "${CURL_MESSAGE}" | grep -Po '"branchPath":.*?[^\\]"' | sed 's/\"branchPath\":\"\(.*\)\"/\1/')

	echo_date "Identified '${CODE_SYSTEM_BRANCH_PATH}' as the current branch path of '${CODE_SYSTEM_SHORT_NAME}'"

}

get_code_system_default_module_id() {

	CODE_SYSTEM_DEFAULT_MODULE_ID=$(echo "${CURL_MESSAGE}" | grep -Po '"defaultModuleId":.*?[^\\]"' | sed 's/\"defaultModuleId\":\"\(.*\)\"/\1/')

	if [ ! -z "${CODE_SYSTEM_DEFAULT_MODULE_ID}" ]; then
		echo_date "Identified '${CODE_SYSTEM_DEFAULT_MODULE_ID}' as the default module ID of '${CODE_SYSTEM_SHORT_NAME}'"
	else
		echo_date "No default module ID is set for '${CODE_SYSTEM_SHORT_NAME}'"
	fi

}

get_code_system_metadata() {

	METADATA_JSON=$(echo "${CURL_MESSAGE}" | grep -Po '"metadata":.*?},"name"')
	CODE_SYSTEM_METADATA=${METADATA_JSON::-7}

	if [ "${CODE_SYSTEM_METADATA}" == "\"metadata\":{}" ]; then
		echo_date "No metadata is set for '${CODE_SYSTEM_SHORT_NAME}'"
	else
		echo_date "Metadata of '${CODE_SYSTEM_SHORT_NAME}' is '${CODE_SYSTEM_METADATA}'"
	fi

}

get_snapshot_export() {

	CODE_SYSTEM_TMP_DIR="${TMP_DIR}/${CODE_SYSTEM_SHORT_NAME}"
	mkdir "${CODE_SYSTEM_TMP_DIR}"

	FINAL_EXPORT_ARCHIVE_PATH=""

	if [ ! -z "${CODE_SYSTEM_DEFAULT_MODULE_ID}" ]; then
		${PATH_TO_EXPORT_SCRIPT} -u "${SNOWOWL_USER}" -p "${SNOWOWL_USER_PASSWORD}" -b "${BASE_URL}" -t "${CODE_SYSTEM_TMP_DIR}" -e "SNAPSHOT" -m "${CODE_SYSTEM_DEFAULT_MODULE_ID}" -s "${CODE_SYSTEM_BRANCH_PATH}" -a "${SNOMED_BASE_URL}"
	else
		${PATH_TO_EXPORT_SCRIPT} -u "${SNOWOWL_USER}" -p "${SNOWOWL_USER_PASSWORD}" -b "${BASE_URL}" -t "${CODE_SYSTEM_TMP_DIR}" -e "SNAPSHOT" -s "${CODE_SYSTEM_BRANCH_PATH}" -a "${SNOMED_BASE_URL}"
	fi

	EXPORT_ARCHIVE=$(find "${CODE_SYSTEM_TMP_DIR}" -maxdepth 1 -mindepth 1 -type f -name "*.zip")

	if [ ! -f "${EXPORT_ARCHIVE}" ]; then
		echo_error "Can't find export archive for '${CODE_SYSTEM_SHORT_NAME}'"
	else

		DATE=$(date +"%Y%m%d%H%M%S")
		FORMATTED_BRANCHPATH=$(echo ${CODE_SYSTEM_BRANCH_PATH} | sed -r 's/\/+/_/g')

		if [ ! -z "${ENVIRONMENT_PREFIX}" ]; then
			FINAL_EXPORT_ARCHIVE="${ENVIRONMENT_PREFIX}_${FORMATTED_BRANCHPATH}_${DATE}.zip"
		else
			FINAL_EXPORT_ARCHIVE="${FORMATTED_BRANCHPATH}_${DATE}.zip"
		fi

		FINAL_EXPORT_ARCHIVE_PATH="${CODE_SYSTEM_TMP_DIR}/${FINAL_EXPORT_ARCHIVE}"

		mv "${EXPORT_ARCHIVE}" "${FINAL_EXPORT_ARCHIVE_PATH}" && echo_date "Final export archive can be found @ '${FINAL_EXPORT_ARCHIVE_PATH}'"

	fi

}

upload_to_s3_bucket() {

	[[ "${S3_BUCKET}" != */ ]] && S3_BUCKET="${S3_BUCKET}/"

	echo_step "Upload SNAPSHOT package to S3 bucket '${S3_BUCKET}'"

	aws s3 cp "${FINAL_EXPORT_ARCHIVE_PATH}" "${S3_BUCKET}${FINAL_EXPORT_ARCHIVE}"

}

update_branch_metadata() {

	echo_step "Update branch metadata"

	if [ "${CODE_SYSTEM_METADATA}" == "\"metadata\":{}" ]; then
		CODE_SYSTEM_METADATA="\"metadata\":{\"previousPackage\":\"${FINAL_EXPORT_ARCHIVE}\"}"
	else
		CODE_SYSTEM_METADATA=$(echo "${CODE_SYSTEM_METADATA}" | sed 's/\"previousPackage\":\"\(.*\).zip\",/\"previousPackage\":\"'${FINAL_EXPORT_ARCHIVE}'\",/')
	fi

	echo_date "Metadata of '${CODE_SYSTEM_SHORT_NAME}' will be updated to '${CODE_SYSTEM_METADATA}'"

	PUT_REQUEST="{${CODE_SYSTEM_METADATA}}"

	rest_put "${PUT_REQUEST}" "${BASE_URL}${SNOMED_BASE_URL}${BRANCHES_URL}/${CODE_SYSTEM_BRANCH_PATH}"

	if [ "${CURL_HTTP_STATUS}" != "204" ]; then
		echo_error "Failed to update metadata of '${CODE_SYSTEM_SHORT_NAME}'. HTTP response code: '${CURL_HTTP_STATUS}'"
	else
		echo_date "Branch metadata successfully updated for '${CODE_SYSTEM_SHORT_NAME}' @ '${CODE_SYSTEM_BRANCH_PATH}'"
	fi

}

cleanup() {

	if [ -d "${TMP_DIR}" ]; then

		echo_step "Clean up"
		rm --recursive --force ${TMP_DIR} && echo_date "Deleted temporary dir @ '${TMP_DIR}'"

	fi

}

main() {

	check_variables

	TMP_DIR=$(mktemp -d)

	for CODE_SYSTEM_SHORT_NAME in "${CODE_SYSTEMS_TO_UPDATE[@]}"; do

		rest_get "${BASE_URL}${ADMIN_BASE_URL}${CODE_SYSTEMS_URL}/${CODE_SYSTEM_SHORT_NAME}"

		if [ "${CURL_HTTP_STATUS}" != "200" ]; then
			echo_error "Unknown code system with short name '${CODE_SYSTEM_SHORT_NAME}'"
		else

			echo_step "'${CODE_SYSTEM_SHORT_NAME}'"

			get_code_system_branch_path

			rest_get "${BASE_URL}${SNOMED_BASE_URL}${BRANCHES_URL}/${CODE_SYSTEM_BRANCH_PATH}"

			if [ "${CURL_HTTP_STATUS}" != "200" ]; then
				echo_error "Unknown code system branch path '${CODE_SYSTEM_BRANCH_PATH}'"
			else

				get_code_system_default_module_id
				get_code_system_metadata

				echo_step "Export code system '${CODE_SYSTEM_SHORT_NAME}'"

				get_snapshot_export

				if [ ! -f "${FINAL_EXPORT_ARCHIVE_PATH}" ]; then
					echo_error "Couldn't find final export archive for '${CODE_SYSTEM_SHORT_NAME}'"
				else

					upload_to_s3_bucket
					update_branch_metadata

				fi

			fi

		fi

	done

	exit 0

}

trap cleanup EXIT

while getopts ":hu:p:r:s:x:e:c:" opt; do
	case "$opt" in
	h)
		usage
		exit 0
		;;
	u)
		SNOWOWL_USER=$OPTARG
		;;
	p)
		SNOWOWL_USER_PASSWORD=$OPTARG
		;;
	r)
		BASE_URL=$OPTARG
		;;
	s)
		S3_BUCKET=$OPTARG
		;;
	x)
		PATH_TO_EXPORT_SCRIPT=$OPTARG
		;;
	e)
		ENVIRONMENT_PREFIX=$OPTARG
		;;
	c)
		CODE_SYSTEMS_TO_UPDATE+=("${OPTARG}")
		;;
	\?)
		echo_error "Invalid option: $OPTARG" >&2
		usage
		exit 1
		;;
	:)
		echo_error "Option: -$OPTARG requires an argument." >&2
		usage
		exit 1
		;;
	esac
done

main
