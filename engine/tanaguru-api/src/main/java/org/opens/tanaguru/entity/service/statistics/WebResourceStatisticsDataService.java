/*
 * Tanaguru - Automated webpage assessment
 * Copyright (C) 2008-2011  Open-S Company
 *
 * This file is part of Tanaguru.
 *
 * Tanaguru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: open-s AT open-s DOT com
 */
package org.opens.tanaguru.entity.service.statistics;

import org.opens.tanaguru.entity.statistics.WebResourceStatistics;
import org.opens.tanaguru.entity.audit.TestSolution;
import org.opens.tanaguru.entity.subject.WebResource;
import org.opens.tanaguru.sdk.entity.service.GenericDataService;

/**
 *
 * @author jkowalczyk
 */
public interface WebResourceStatisticsDataService
        extends GenericDataService<WebResourceStatistics, Long>{

    /**
     * 
     * @param webresourceId
     * @param testSolution
     * @return
     */
    Long getResultCountByResultType(
            Long webresourceId,
            TestSolution testSolution);

    /**
     * 
     * @param parentWebresourceId
     * @param testSolution
     * @return
     */
    Long getNumberOfOccurrencesByWebResourceAndResultType(
            Long webresourceId,
            TestSolution testSolution);

    /**
     *
     * @param webresourceId
     * @return
     */
    Integer getHttpStatusCodeByWebResource(Long webresourceId);
    
    /**
     *
     * @param webresourceId
     * @return
     */
    WebResourceStatistics getWebResourceStatisticsByWebResource(WebResource webResource);
}
