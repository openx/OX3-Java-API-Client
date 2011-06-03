/*======================================================================*
 * Copyright (c) 2011, OpenX Technologies, Inc. All rights reserved.    *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License. Unless required     *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/
package com.openx.ox3.entities;

/**
 *
 * @author keithmiller
 */
public class OX3Account
{
    private int id;
    private String name;
    private String status;
    private int account_id;
    private int account_type_id;
    private int master;
    private int currency_id;
    private int timezone_id;
    private String country_of_business_id;
    private int single_ad_limitation;
    private int primary_contact_id;
    private int billing_contact_id;
    private String external_id;
    private String notes;
    private int instance_id;
    private String modified_date;
    private String created_date;
    private int deleted;
    private int market_active;
    private int market_currency_id;
    
    OX3Account() {
        
    }
    
    public int getAccount_id() {
        return account_id;
    }

    public int getAccount_type_id() {
        return account_type_id;
    }

    public int getBilling_contact_id() {
        return billing_contact_id;
    }

    public String getCountry_of_business_id() {
        return country_of_business_id;
    }

    public String getCreated_date() {
        return created_date;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public int getDeleted() {
        return deleted;
    }

    public String getExternal_id() {
        return external_id;
    }

    public int getId() {
        return id;
    }

    public int getInstance_id() {
        return instance_id;
    }

    public int getMarket_active() {
        return market_active;
    }

    public int getMarket_currency_id() {
        return market_currency_id;
    }

    public int getMaster() {
        return master;
    }

    public String getModified_date() {
        return modified_date;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public int getPrimary_contact_id() {
        return primary_contact_id;
    }

    public int getSingle_ad_limitation() {
        return single_ad_limitation;
    }

    public String getStatus() {
        return status;
    }

    public int getTimezone_id() {
        return timezone_id;
    }
}
