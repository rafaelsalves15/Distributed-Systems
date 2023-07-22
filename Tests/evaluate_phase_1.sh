#!/bin/bash

#####################################################################################################################
#                                            INSTRUCTIONS                                                           #
#####################################################################################################################
#1. Run this file in a new directory, where all the projects will be cloned.                                        #
#2. The new directory should only contain evaluate_phase_1.sh and tests_phase_1/                                    #
#3. The test files and expected results are in the tests_phase_1/ directory                                         #
#4. Note that we only run one server, that preserves state across tests, so they are dependent on each other        #
#5. Before running the script update the group numbers.                                                             #
#6. Run the script `.evaluate_phase_1.sh`, it will clone the repositories, compile everything and run the tests     #
#7. The final results for group GXX will be in results_phase_1/GXX-DistLedger/all_tests.diff                        #
#####################################################################################################################

#####################################################################################################################
#                                         TESTS DESCRIPTION                                                         #
#####################################################################################################################
# USER 1: alice creates account, gets balance, transfers from broker 150, and checks final balance                  #
# USER 2: bob creates account, alice transfers 100 to bob, checks alice's balance, checks bob's balance             #
# USER 3: alice deletes account -> throws error, alice transfers 50 to bob, alice deletes account, bob checks balanc#
#         alice creates account, alice transfers 100 to bob -> throws error, bob creates account,                   #
#         bob transfers 100 to bob -> throws error, bob transfers -10 to alice -> throws error                      #
#         bob transfers 0 to alice, bob transfers 10 to charlie -> throws error                                     #
#         charlie transfers 10 to bob -> throws error, checks bob's balance, checks alice's balance                 #
# ADMIN 1: admin gets ledger state                                                                                  #
# ADMIN 2: admin deactivates server                                                                                 #
# USER 4: bob gets balance -> fails, bob transfers 10 to alice -> fails, charlie creates account                    #
#         alice deletes account -> fails                                                                            #
#####################################################################################################################

#groups="06 12 13 14 16 17 18 34 35 36 37 38 39 40 49 61 62"
groups="XX YY ZZ"
campi="A"

# Iterate the string variable using for loop
for group in $groups; do
    clone repo for group
    git clone https://github.com/tecnico-distsys/$campi$group-DistLedger.git
    echo "cloned repo for $campi$group"

    groupTestsDir=results_phase_1/$campi$group-DistLedger
    mkdir results_phase_1/
    mkdir $groupTestsDir
    touch $groupTestsDir/all_tests.diff | echo -n "" > $groupTestsDir/all_tests.diff

    # For Macs with M1 - comment otherwise
    # sed -i '' -e 's/${os.detected.classifier}/osx-x86_64/g' "$campi$group-DistLedger/Contract/pom.xml"

    # compile code
    cd $campi$group-DistLedger/
    git fetch --all --tags
    git checkout tags/SD_P1
    mvn clean compile install -DskipTests

    # init DistLedgerServer
    cd DistLedgerServer/
    mvn exec:java &

    # go bak to tests root
    cd ../../

    # for each user test file
    for i in {1..3}
    do
        cd $campi$group-DistLedger/User/
        mvn exec:java < ../../tests_phase_1/user_test$i.txt &> ../../$groupTestsDir/user_test$i.txt.out

        cd ../../

        # clean up - delete [INFO] prints
        awk '!/\[INFO\]/' $groupTestsDir/user_test$i.txt.out > $groupTestsDir/user_test$i.out
        rm $groupTestsDir/user_test$i.txt.out

        diff $groupTestsDir/user_test$i.out tests_phase_1/user_test$i.expected > $groupTestsDir/user_test$i.diff
    done

    # for each admin test file
    for i in {1..2}
    do
        cd $campi$group-DistLedger/Admin/
        mvn exec:java < ../../tests_phase_1/admin_test$i.txt &> ../../$groupTestsDir/admin_test$i.txt.out

        cd ../../

        # clean up - delete [INFO] prints
        awk '!/\[INFO\]/' $groupTestsDir/admin_test$i.txt.out > $groupTestsDir/admin_test$i.out
        rm $groupTestsDir/admin_test$i.txt.out

        diff $groupTestsDir/admin_test$i.out tests_phase_1/admin_test$i.expected > $groupTestsDir/admin_test$i.diff
    done

    # user tries to make request but server is deactivated

    # for each user test file
    for i in {4..4}
    do
        cd $campi$group-DistLedger/User/
        mvn exec:java < ../../tests_phase_1/user_test$i.txt &> ../../$groupTestsDir/user_test$i.txt.out

        cd ../../

        # clean up - delete [INFO] prints
        awk '!/\[INFO\]/' $groupTestsDir/user_test$i.txt.out > $groupTestsDir/user_test$i.out
        rm $groupTestsDir/user_test$i.txt.out

        diff $groupTestsDir/user_test$i.out tests_phase_1/user_test$i.expected > $groupTestsDir/user_test$i.diff
    done

    # group all group results in one diff file
    tail -n 100 $groupTestsDir/user_test1.diff $groupTestsDir/user_test2.diff $groupTestsDir/user_test3.diff $groupTestsDir/admin_test1.diff $groupTestsDir/admin_test2.diff $groupTestsDir/user_test4.diff > $groupTestsDir/all_tests.diff

    # kill server process
    kill $(lsof -i -P -n | grep 2001 | grep java | awk '{print $2}')

    echo $campi$group evaluation completed. Check $groupTestsDir/user_test1.diff..................
    sleep 4
done
