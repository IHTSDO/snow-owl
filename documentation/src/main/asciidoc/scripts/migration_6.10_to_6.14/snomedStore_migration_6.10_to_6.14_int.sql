USE snomedStore;

DROP TABLE IF EXISTS `snomedrefset_snomedconcretedatatyperefsetmember`;
CREATE TABLE `snomedrefset_snomedconcretedatatyperefsetmember` (
  `cdo_id` bigint(20) NOT NULL,
  `cdo_version` int(11) NOT NULL,
  `cdo_branch` int(11) NOT NULL,
  `cdo_created` bigint(20) NOT NULL,
  `cdo_revised` bigint(20) NOT NULL,
  `cdo_resource` bigint(20) NOT NULL,
  `cdo_container` bigint(20) NOT NULL,
  `cdo_feature` int(11) NOT NULL,
  `effectiveTime` timestamp NULL DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `refSet` bigint(20) DEFAULT NULL,
  `released` tinyint(1) DEFAULT NULL,
  `referencedComponentId` varchar(2048) DEFAULT NULL,
  `moduleId` varchar(2048) DEFAULT NULL,
  `uuid` varchar(2048) DEFAULT NULL,
  `group0` int(11) DEFAULT NULL,
  `serializedValue` varchar(2048) DEFAULT NULL,
  `typeId` varchar(2048) DEFAULT NULL,
  `characteristicTypeId` varchar(2048) DEFAULT NULL,
  `cdo_set_effectiveTime` tinyint(1) DEFAULT NULL,
  UNIQUE KEY `snomedrefset_SnomedConcreteDataTypeRefSetMember_idx0` (`cdo_id`,`cdo_version`,`cdo_branch`),
  KEY `snomedrefset_SnomedConcreteDataTypeRefSetMember_idx1` (`cdo_id`,`cdo_revised`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `mrcm_concretedomainelementpredicate`;
CREATE TABLE `mrcm_concretedomainelementpredicate` (
  `cdo_id` bigint(20) NOT NULL,
  `cdo_version` int(11) NOT NULL,
  `cdo_branch` int(11) NOT NULL,
  `cdo_created` bigint(20) NOT NULL,
  `cdo_revised` bigint(20) NOT NULL,
  `cdo_resource` bigint(20) NOT NULL,
  `cdo_container` bigint(20) NOT NULL,
  `cdo_feature` int(11) NOT NULL,
  `uuid` varchar(2048) DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `effectiveTime` timestamp NULL DEFAULT NULL,
  `author` varchar(2048) DEFAULT NULL,
  `attribute` bigint(20) DEFAULT NULL,
  `range0` varchar(2048) DEFAULT NULL,
  `characteristicTypeConceptId` varchar(2048) DEFAULT NULL,
  UNIQUE KEY `mrcm_ConcreteDomainElementPredicate_idx0` (`cdo_id`,`cdo_version`,`cdo_branch`),
  KEY `mrcm_ConcreteDomainElementPredicate_idx1` (`cdo_id`,`cdo_revised`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


UPDATE `cdo_external_refs` SET `uri` = "http://b2international.com/snowowl/snomed/refset/1.0#//SnomedConcreteDataTypeRefSetMember/group" WHERE `id` = -57;
UPDATE `cdo_external_refs` SET `uri` = "http://b2international.com/snowowl/snomed/refset/1.0#//SnomedConcreteDataTypeRefSetMember/typeId" WHERE `id` = -58;

DELETE FROM `cdo_external_refs` WHERE `uri`="http://b2international.com/snowowl/snomed/mrcm#//ConcreteDomainElementPredicate/name";

UPDATE `cdo_external_refs` SET `uri` = "http://b2international.com/snowowl/snomed/mrcm#//ConcreteDomainElementPredicate/attribute" WHERE `id` = -134;
UPDATE `cdo_external_refs` SET `uri` = "http://b2international.com/snowowl/snomed/mrcm#//ConcreteDomainElementPredicate/range" WHERE `id` = -135;
