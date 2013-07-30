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
 
 Java Demo for using OAuth to interact with the OX3 API

CONFIGURING FOR YOUR OX3 INSTANCE:
As the configurations are held in a .properties file, you may choose to set the
configs before or after compilation, depending on your build/deploy cycle. With
the source files, default.properties can be found in src/main/resources. In the
build product, default.properties can be found in the top directory.

BUILDING THE JAR:
To build to target/OAuthDemo-1.1.0.jar and
target/OAuthDemo-1.1.0-jar-with-dependencies.jar run the following here:

mvn package

USING THIS PACKAGE TO INTERACT WITH OX3:
Examples of making GET requests against your OX3 instance can be found in
src/main/java/com/openx/oauthdemo/Demo.java and DemoV2.java in the same path.
Requests using other HTTP methods are not yet implemented by the Helper class.
To make arbitrary requests against your OX3 instance, log in to OX3 and get your
client's cookie jar like so:

import com.openx.oauth.client.Client;
import org.apache.http.impl.client.BasicCookieStore;
...
Client client = new Client( ...your OX3 creds... );
client.OX3OAuth();
BasicCookieStore cookieJar = client.getHelper().getCookieStore();

API V1 VERSUS V2:
If you are using V2, remember to send requests where there is data (POST and
PUT) in JSON format. This also means you must set the Content-Type header to
application/json.
