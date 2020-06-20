# PBL 研修環境の構築

このドキュメントでは PBL 用の研修環境の構築手順について説明します。

## リソースプロバイダの確認

以下のコマンドを実行し、AKS に必要なリソース・プロバイダーを有効にします。

```
$ az provider register --namespace Microsoft.Network
$ az provider register --namespace Microsoft.Storage
$ az provider register --namespace Microsoft.Compute
$ az provider register --namespace Microsoft.ContainerService
```

以下のコマンドを実行して、リソースプロバイダが Registered となっていることを確認します。

```
$ az provider list -o table
Namespace                               RegistrationPolicy    RegistrationState
--------------------------------------  --------------------  -------------------
Microsoft.Network                       RegistrationRequired  Registered
Microsoft.Storage                       RegistrationRequired  Registered
Microsoft.Compute                       RegistrationRequired  Registered
Microsoft.ContainerService              RegistrationRequired  Registered
84codes.CloudAMQP                       RegistrationRequired  NotRegistered
Conexlink.MyCloudIT                     RegistrationRequired  NotRegistered
　　　　　　　　　　　　　　　　　　　　　　:
```

## Azure Containers Registory (ACR) の作成

### リソースグループ作成

ACR を作る前にリソースグループを作成する必要があります。リソースグループは、Azure 内でユニークである必要はなく、
皆さんのテナント内でユニークであれば問題ありません。以下のコマンドで、リソースグループを作成します。

```
$ az group create \
              --resource-group team0-resource-group \
              --location japaneast
{
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group",
  "location": "japaneast",
  "managedBy": null,
  "name": "team0-resource-group",
  "properties": {
    "provisioningState": "Succeeded"
  },
  "tags": null,
  "type": "Microsoft.Resources/resourceGroups"
}
```

### ACR 名の確認

ACR のレジストリ名は Azure 内でユニークである必要があります。まずは、以下のコマンドで ACR 名が利用可能であるか確認してください
ACR 名は英数字としてください。nameAvailable が true であれば利用可能です。

```
$ az acr check-name -n team0registory
{
  "message": null,
  "nameAvailable": true,
  "reason": null
}
```

### ACR の作成

作成したリソースグループに ACR を作成します。ACR の名前には、先程、利用可能であることを確認した名前を指定してください。

```
$ az acr create \
             --name team0registory \
             --resource-group team0-resource-group \
             --sku Standard \
             --location japaneast
{- Finished ..
  "adminUserEnabled": false,
  "creationDate": "2020-06-20T14:51:06.923596+00:00",
  "dataEndpointEnabled": false,
  "dataEndpointHostNames": [],
  "encryption": {
    "keyVaultProperties": null,
    "status": "disabled"
  },
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.ContainerRegistry/registries/team0registory",
  "identity": null,
  "location": "japaneast",
  "loginServer": "team0registory.azurecr.io",
  "name": "team0registory",
  "networkRuleSet": null,
  "policies": {
    "quarantinePolicy": {
      "status": "disabled"
    },
    "retentionPolicy": {
      "days": 7,
      "lastUpdatedTime": "2020-06-20T14:51:08.970997+00:00",
      "status": "disabled"
    },
    "trustPolicy": {
      "status": "disabled",
      "type": "Notary"
    }
  },
  "privateEndpointConnections": [],
  "provisioningState": "Succeeded",
  "publicNetworkAccess": "Enabled",
  "resourceGroup": "team0-resource-group",
  "sku": {
    "name": "Standard",
    "tier": "Standard"
  },
  "status": null,
  "storageAccount": null,
  "tags": {},
  "type": "Microsoft.ContainerRegistry/registries"
}
```

## Azure Kubernetes Service (AKS) 作成

### AKS の Virtual Network の作成

AKS が使用する Virtual Network を作成します。以下のコマンドで、先程作成した AKS 用のリソースグループを指定して作成します。

```
$ az network vnet create \
               --name aks-team0-vnet \
               --resource-group team0-resource-group \
               --location japaneast \
               --address-prefixes 10.1.0.0/22
{
  "newVNet": {
    "addressSpace": {
      "addressPrefixes": [
        "10.1.0.0/22"
      ]
    },
    "bgpCommunities": null,
    "ddosProtectionPlan": null,
    "dhcpOptions": {
      "dnsServers": []
    },
    "enableDdosProtection": false,
    "enableVmProtection": false,
    "etag": "W/\"6e5139b4-76c9-40a7-9d04-89c72fca8019\"",
    "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet",
    "ipAllocations": null,
    "location": "japaneast",
    "name": "aks-team0-vnet",
    "provisioningState": "Succeeded",
    "resourceGroup": "team0-resource-group",
    "resourceGuid": "50280d47-3ad2-4174-9641-f7faab92732e",
    "subnets": [],
    "tags": {},
    "type": "Microsoft.Network/virtualNetworks",
    "virtualNetworkPeerings": []
  }
}
```

### サービスプリンシパルの作成

以下のコマンドでサービスプリンシパルを作成します。

```
$ az ad sp create-for-rbac --skip-assignment
{
  "appId": "42a2745a-eaf8-46e6-80bb-a2ee21f1b61c",
  "displayName": "azure-cli-2020-06-20-14-53-25",
  "name": "http://azure-cli-2020-06-20-14-53-25",
  "password": "pJhMOrqYwNaPp30MypdA38DaVVAGjUQ__k",
  "tenant": "bfc99600-59e2-4cc1-981b-361c2f802cf2"
}
```

ここで、実行結果の appId は、後述のロールのアサイン時の --assignee、および AKS クラスタの作成時の --service-principal に指定し、
password は AKS クラスタ作成時の --client-secret に指定するので、実行結果は記録しておいてください。

### VNet ID の確認

以下のコマンドを実行し、aks-team0-vnet の VNet ID を確認します。

```
$ az network vnet show \
               --resource-group team0-resource-group \
               --name aks-team0-vnet \
               --query id -o tsv
/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet
```

### ロールのアサイン

以下のコマンドでロールのアサインを実行します。
--assignee に、サービスプリンシパル作成時の appId、--scope に、aks-team0-vnet の VNet ID を指定することに注意してください。

```
$ az role assignment create \
              --assignee 42a2745a-eaf8-46e6-80bb-a2ee21f1b61c \
              --scope /subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet \
              --role Contributor
{
  "canDelegate": null,
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet/providers/Microsoft.Authorization/roleAssignments/7e7dac4a-da7c-4bd3-9b2b-585971be2e91",
  "name": "7e7dac4a-da7c-4bd3-9b2b-585971be2e91",
  "principalId": "0f42b825-238e-4312-b3f5-1673a9e05a61",
  "principalType": "ServicePrincipal",
  "resourceGroup": "team0-resource-group",
  "roleDefinitionId": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/providers/Microsoft.Authorization/roleDefinitions/b24988ac-6180-42a0-ab88-20f7382dd24c",
  "scope": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet",
  "type": "Microsoft.Authorization/roleAssignments"
}
```

### サポートされている Kubernetes のバージョンの確認

以下のコマンドを実行し、このリージョンでサポートされている Kubernetes のバージョンを確認します。
以下のケースでは 1.16.9 が、最新の stable バージョンです。

```
$ az aks get-versions \
         --location japaneast \
         --output table
KubernetesVersion    Upgrades
-------------------  -------------------------------------------------
1.18.2(preview)      None available
1.18.1(preview)      1.18.2(preview)
1.17.5(preview)      1.18.1(preview), 1.18.2(preview)
1.17.4(preview)      1.17.5(preview), 1.18.1(preview), 1.18.2(preview)
1.16.9               1.17.4(preview), 1.17.5(preview)
1.16.8               1.16.9, 1.17.4(preview), 1.17.5(preview)
1.15.11              1.16.8, 1.16.9
1.15.10              1.15.11, 1.16.8, 1.16.9
1.14.8               1.15.10, 1.15.11
1.14.7               1.14.8, 1.15.10, 1.15.11
```

### AKS 用の Subnet の作成

以下のコマンドを実行して、AKS 用の Subnet を作成します。

```
$ az network vnet subnet create \
                   --name aks-team0-subnet \
                   --resource-group team0-resource-group \
                   --vnet-name aks-team0-vnet \
                   --address-prefix 10.1.1.0/24
{
  "addressPrefix": "10.1.1.0/24",
  "addressPrefixes": null,
  "delegations": [],
  "etag": "W/\"a5b32845-1848-43d5-9288-7a0cf5838e7e\"",
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet/subnets/aks-team0-subnet",
  "ipAllocations": null,
  "ipConfigurationProfiles": null,
  "ipConfigurations": null,
  "name": "aks-team0-subnet",
  "natGateway": null,
  "networkSecurityGroup": null,
  "privateEndpointNetworkPolicies": "Enabled",
  "privateEndpoints": null,
  "privateLinkServiceNetworkPolicies": "Enabled",
  "provisioningState": "Succeeded",
  "purpose": null,
  "resourceGroup": "team0-resource-group",
  "resourceNavigationLinks": null,
  "routeTable": null,
  "serviceAssociationLinks": null,
  "serviceEndpointPolicies": null,
  "serviceEndpoints": null,
  "type": "Microsoft.Network/virtualNetworks/subnets"
}
```

ここで、実行結果の id は、後述の AKS クラスタ作成時の --vnet-subnet-id に指定するので、実行結果は記録しておいてください。

### 利用可能な VM サイズの確認

AKS クラスタに使用する VM のサイズを確認します。以下のコマンドで、このリージョンで利用可能な VM のサイズを確認します。

```
$ az vm list-sizes \
        --location japaneast \
        --output table
MaxDataDiskCount    MemoryInMb    Name                    NumberOfCores    OsDiskSizeInMb    ResourceDiskSizeInMb
------------------  ------------  ----------------------  ---------------  ----------------  ----------------------
24                  57344         Standard_NV6            6                1047552           389120
48                  114688        Standard_NV12           12               1047552           696320
64                  229376        Standard_NV24           24               1047552           1474560
24                  57344         Standard_NV6_Promo      6                1047552           389120
48                  114688        Standard_NV12_Promo     12               1047552           696320
64                  229376        Standard_NV24_Promo     24               1047552           1474560
　　　　　　　　　　　　　　　　　　　　　　:
```

### AKS の作成

以下のコマンドを実行して、AKS クラスタを作成します。
ここで、--vnet-subnet-id には、AKS 用の Subnet の ID、--service-principal と --client-secret には、
サービスプリンシパルの実行結果の appId、および password を指定することに注意します。

```
$ az aks create \
           --name aks-team0-cluster \
           --resource-group team0-resource-group \
           --location japaneast \
           --vnet-subnet-id "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet/subnets/aks-team0-subnet" \
           --generate-ssh-keys \
           --network-plugin "azure" \
           --kubernetes-version 1.16.9 \
           --node-count 3 \
           --node-vm-size Standard_B2ms \
           --max-pods 50 \
           --dns-name-prefix aks-team0-cluster \
           --enable-addons monitoring,http_application_routing \
           --service-principal "42a2745a-eaf8-46e6-80bb-a2ee21f1b61c" \
           --client-secret "pJhMOrqYwNaPp30MypdA38DaVVAGjUQ__k"
AAD role propagation done[############################################]  100.0000%{
  "aadProfile": null,
  "addonProfiles": {
    "KubeDashboard": {
      "config": null,
      "enabled": true,
      "identity": null
    },
    "httpApplicationRouting": {
      "config": {
        "HTTPApplicationRoutingZoneName": "80e08a12b6fa40b4bb55.japaneast.aksapp.io"
      },
      "enabled": true,
      "identity": null
    },
    "omsagent": {
      "config": {
        "logAnalyticsWorkspaceResourceID": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourcegroups/defaultresourcegroup-ejp/providers/microsoft.operationalinsights/workspaces/defaultworkspace-1898747d-2160-4abf-ad79-70c094b0ccd1-ejp"
      },
      "enabled": true,
      "identity": null
    }
  },
  "agentPoolProfiles": [
    {
      "availabilityZones": null,
      "count": 3,
      "enableAutoScaling": null,
      "enableNodePublicIp": false,
      "maxCount": null,
      "maxPods": 50,
      "minCount": null,
      "mode": "System",
      "name": "nodepool1",
      "nodeLabels": {},
      "nodeTaints": null,
      "orchestratorVersion": "1.16.9",
      "osDiskSizeGb": 128,
      "osType": "Linux",
      "provisioningState": "Succeeded",
      "scaleSetEvictionPolicy": null,
      "scaleSetPriority": null,
      "spotMaxPrice": null,
      "tags": null,
      "type": "VirtualMachineScaleSets",
      "vmSize": "Standard_B2ms",
      "vnetSubnetId": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet/subnets/aks-team0-subnet"
    }
  ],
  "apiServerAccessProfile": null,
  "autoScalerProfile": null,
  "diskEncryptionSetId": null,
  "dnsPrefix": "aks-team0-cluster",
  "enablePodSecurityPolicy": null,
  "enableRbac": true,
  "fqdn": "aks-team0-cluster-48a42a1a.hcp.japaneast.azmk8s.io",
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourcegroups/team0-resource-group/providers/Microsoft.ContainerService/managedClusters/aks-team0-cluster",
  "identity": null,
  "identityProfile": null,
  "kubernetesVersion": "1.16.9",
  "linuxProfile": {
    "adminUsername": "azureuser",
    "ssh": {
      "publicKeys": [
        {
          "keyData": "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDCjyVsEkGPh3030VLucq9ijEwtL0MvPujN525Jcp4NYFyx4AAc8GPy7aXHWMYGwIhrVMcicrJEV7J4zbUUo7EgvMmbHdAnN2oif6/QzHG203rLKyArUHSHsRVgx/B3pepgR+Vw4zluM6mg4JXpmyfnIUiyMcOAEi8x8v31Cispssnl3bkAJKf7TNEPI7dIn4PeQX4PI3ksWUKpjWg0XEZyT10LoWptRHrDPuTuFzNaRRyZSr7r8Y3QlOYjmPrB7n9tSx8DJEY8tsONxOEr0c4f82tN4/azHPa2dNyE4cPS1HwIUYZP8dmehzxUwsFnmFMtVvfcKTRMfC8WrrAEFESX tatsutas40@COAMAC14100266.local\n"
        }
      ]
    }
  },
  "location": "japaneast",
  "maxAgentPools": 10,
  "name": "aks-team0-cluster",
  "networkProfile": {
    "dnsServiceIp": "10.0.0.10",
    "dockerBridgeCidr": "172.17.0.1/16",
    "loadBalancerProfile": {
      "allocatedOutboundPorts": null,
      "effectiveOutboundIps": [
        {
          "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/MC_team0-resource-group_aks-team0-cluster_japaneast/providers/Microsoft.Network/publicIPAddresses/5e380061-d876-438b-b9d3-cdc3420fd552",
          "resourceGroup": "MC_team0-resource-group_aks-team0-cluster_japaneast"
        }
      ],
      "idleTimeoutInMinutes": null,
      "managedOutboundIps": {
        "count": 1
      },
      "outboundIpPrefixes": null,
      "outboundIps": null
    },
    "loadBalancerSku": "Standard",
    "networkMode": null,
    "networkPlugin": "azure",
    "networkPolicy": null,
    "outboundType": "loadBalancer",
    "podCidr": null,
    "serviceCidr": "10.0.0.0/16"
  },
  "nodeResourceGroup": "MC_team0-resource-group_aks-team0-cluster_japaneast",
  "privateFqdn": null,
  "provisioningState": "Succeeded",
  "resourceGroup": "team0-resource-group",
  "servicePrincipalProfile": {
    "clientId": "42a2745a-eaf8-46e6-80bb-a2ee21f1b61c",
    "secret": null
  },
  "sku": {
    "name": "Basic",
    "tier": "Free"
  },
  "tags": null,
  "type": "Microsoft.ContainerService/ManagedClusters",
  "windowsProfile": {
    "adminPassword": null,
    "adminUsername": "azureuser"
  }
}
```

AKS クラスタの作成には、10 - 30 分程度時間がかかります。

### kubectl 用のクラスタの認証情報の取得

以下のコマンドを実行して、作成した AKS クラスタの認証情報を取得し、kubectl でクラスタを認識できるように構成します。

```
$ az aks get-credentials \
         --name aks-team0-cluster \
         --resource-group team0-resource-group
```

以下のコマンドを実行し、クラスタを構成するノードが表示されば、構築は完了です。

```
$ kubectl get node
NAME                                STATUS   ROLES   AGE     VERSION
aks-nodepool1-13017820-vmss000000   Ready    agent   5m17s   v1.16.9
aks-nodepool1-13017820-vmss000001   Ready    agent   5m12s   v1.16.9
aks-nodepool1-13017820-vmss000002   Ready    agent   5m5s    v1.16.9
```

### ACR のアクセスキーの有効化

Azure Portal にログインし、画面上の検索バーで「コンテナ」と入力し、検索を実行します。検索結果の「コンテナー レジストリ」をクリックします。

![ACR1](acr_access_key1.png)

作成した名前でコンテナーレジストリが表示されるので、リンクをクリックします。

![ACR2](acr_access_key2.png)

左メニューの「設定」-「アクセスキー」をクリックします。

![ACR3](acr_access_key3.png)

管理ユーザーが「無効」となっていることを確認し、「有効」に変更します。

![ACR4](acr_access_key4.png)

ログインサーバー名、ユーザー名、パスワード (password) を記録します。

![ACR5](acr_access_key5.png)

### ACR の紐付け

以下のコマンドを実行して、AKS クラスタと ACR を紐づけします。
--docker-server にログインサーバー名、--docker-username にユーザー名、--docker-password にパスワードを指定します。
--docker-email には G メールアドレスを入力してください。

```
$ kubectl create secret docker-registry docker-reg-credential \
          --docker-server=team0registory.azurecr.io \
          --docker-username=team0registory \
          --docker-password="wDbge0sXdCLUt3hxQ5mv7VBwNrw8J3/Z" \
          --docker-email=shigeru.sb.it@gmail.com

secret/docker-reg-credential created
```

以下のコマンドを実行して、docker-reg-credential が表示されていることを確認します。

```
$ kubectl get secret
NAME                    TYPE                                  DATA   AGE
default-token-mvnns     kubernetes.io/service-account-token   3      19m
docker-reg-credential   kubernetes.io/dockerconfigjson        1      10m
```

## Azure Cache for Redis の作成

### Azure Cache for Redis の作成

以下のコマンドを実行して、Azure Cache for Redis を作成します。

```
$ az redis create \
        --name team0-redis \
        --resource-group team0-resource-group \
        --location japaneast \
        --sku Standard \
        --vm-size c1 \
        --enable-non-ssl-port
{- Finished ..
  "accessKeys": null,
  "enableNonSslPort": true,
  "hostName": "team0-redis.redis.cache.windows.net",
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Cache/Redis/team0-redis",
  "instances": [
    {
      "isMaster": false,
      "nonSslPort": 13000,
      "shardId": null,
      "sslPort": 15000,
      "zone": null
    },
    {
      "isMaster": false,
      "nonSslPort": 13001,
      "shardId": null,
      "sslPort": 15001,
      "zone": null
    }
  ],
  "linkedServers": [],
  "location": "Japan East",
  "minimumTlsVersion": null,
  "name": "team0-redis",
  "port": 6379,
  "provisioningState": "Creating",
  "redisConfiguration": {
    "maxclients": "1000",
    "maxfragmentationmemory-reserved": "50",
    "maxmemory-delta": "50",
    "maxmemory-reserved": "50"
  },
  "redisVersion": "4.0.14",
  "replicasPerMaster": null,
  "resourceGroup": "team0-resource-group",
  "shardCount": null,
  "sku": {
    "capacity": 1,
    "family": "C",
    "name": "Standard"
  },
  "sslPort": 6380,
  "staticIp": null,
  "subnetId": null,
  "tags": {},
  "tenantSettings": {},
  "type": "Microsoft.Cache/Redis",
  "zones": null
}
```

## Azure SQL Database の作成

### SQL Server の作成

以下のコマンドを実行して、SQL Server を作成します。

```
$ az sql server create \
        --name team0-sqlsrv \
        --resource-group team0-resource-group \
        --location japaneast \
        --admin-user ServerAdmin \
        --admin-password welcome1#
{- Finished ..
  "administratorLogin": "ServerAdmin",
  "administratorLoginPassword": null,
  "fullyQualifiedDomainName": "team0-sqlsrv.database.windows.net",
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Sql/servers/team0-sqlsrv",
  "identity": null,
  "kind": "v12.0",
  "location": "japaneast",
  "minimalTlsVersion": null,
  "name": "team0-sqlsrv",
  "privateEndpointConnections": [],
  "publicNetworkAccess": "Enabled",
  "resourceGroup": "team0-resource-group",
  "state": "Ready",
  "tags": null,
  "type": "Microsoft.Sql/servers",
  "version": "12.0"
}
```

### SQL Server の作成

以下のコマンドを実行して、SQL Database を作成します。--server には、先程作成した SQL Server の
サーバ名を指定します。

```
$ az sql db create \
        --resource-group team0-resource-group \
        --server team0-sqlsrv \
        --name team0db \
        --service-objective S2 \
        --collation Japanese_XJIS_100_CI_AS
{- Finished ..
  "autoPauseDelay": null,
  "catalogCollation": "SQL_Latin1_General_CP1_CI_AS",
  "collation": "Japanese_XJIS_100_CI_AS",
  "createMode": null,
  "creationDate": "2020-06-20T15:16:58.707000+00:00",
  "currentServiceObjectiveName": "S2",
  "currentSku": {
    "capacity": 50,
    "family": null,
    "name": "Standard",
    "size": null,
    "tier": "Standard"
  },
  "databaseId": "b36046e0-058e-4ceb-9f06-5844948c5efa",
  "defaultSecondaryLocation": "japanwest",
  "earliestRestoreDate": "2020-06-20T15:46:58.707000+00:00",
  "edition": "Standard",
  "elasticPoolId": null,
  "elasticPoolName": null,
  "failoverGroupId": null,
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Sql/servers/team0-sqlsrv/databases/team0db",
  "kind": "v12.0,user",
  "licenseType": null,
  "location": "japaneast",
  "longTermRetentionBackupResourceId": null,
  "managedBy": null,
  "maxLogSizeBytes": null,
  "maxSizeBytes": 268435456000,
  "minCapacity": null,
  "name": "team0db",
  "pausedDate": null,
  "readReplicaCount": 0,
  "readScale": "Disabled",
  "recoverableDatabaseId": null,
  "recoveryServicesRecoveryPointId": null,
  "requestedServiceObjectiveName": "S2",
  "resourceGroup": "team0-resource-group",
  "restorableDroppedDatabaseId": null,
  "restorePointInTime": null,
  "resumedDate": null,
  "sampleName": null,
  "sku": {
    "capacity": 50,
    "family": null,
    "name": "Standard",
    "size": null,
    "tier": "Standard"
  },
  "sourceDatabaseDeletionDate": null,
  "sourceDatabaseId": null,
  "status": "Online",
  "tags": null,
  "type": "Microsoft.Sql/servers/databases",
  "zoneRedundant": false
}
```

## Virtual Machine の作成

### Virtual Machine 用のサブネットの作成

```
$ az network vnet subnet create \
      --name vm-team0-vnet \
      --resource-group team0-resource-group \
      --vnet-name aks-team0-vnet \
      --address-prefix 10.1.2.0/27
{
  "addressPrefix": "10.1.2.0/27",
  "addressPrefixes": null,
  "delegations": [],
  "etag": "W/\"324b40a6-7183-426a-9675-cd050543f70a\"",
  "id": "/subscriptions/1898747d-2160-4abf-ad79-70c094b0ccd1/resourceGroups/team0-resource-group/providers/Microsoft.Network/virtualNetworks/aks-team0-vnet/subnets/vm-team0-vnet",
  "ipAllocations": null,
  "ipConfigurationProfiles": null,
  "ipConfigurations": null,
  "name": "vm-team0-vnet",
  "natGateway": null,
  "networkSecurityGroup": null,
  "privateEndpointNetworkPolicies": "Enabled",
  "privateEndpoints": null,
  "privateLinkServiceNetworkPolicies": "Enabled",
  "provisioningState": "Succeeded",
  "purpose": null,
  "resourceGroup": "team0-resource-group",
  "resourceNavigationLinks": null,
  "routeTable": null,
  "serviceAssociationLinks": null,
  "serviceEndpointPolicies": null,
  "serviceEndpoints": null,
  "type": "Microsoft.Network/virtualNetworks/subnets"
}
```

## Virtual Machine の作成

- Virtual Machine を作成する
- Virtual Machine のパスワードを有効にする
- SQL Server のファイアウォールで AKS と VM のサブネットを追加する
- Virtual Machine に Azure CLI、sqlcmd をインストールする

https://docs.microsoft.com/ja-jp/cli/azure/install-azure-cli-apt?view=azure-cli-latest
https://docs.microsoft.com/ja-jp/sql/linux/sql-server-linux-setup-tools?view=sql-server-ver15#ubuntu
