def getPubCloudSlave(Map args){
  common = load './rpc-gating/pipeline-steps/common.groovy'
  build_name = ""
  common.conditionalStage(
    stage_name: 'Allocate Resources',
    stage: {
      def allocate = load 'rpc-gating/pipeline-steps/allocate_pubcloud.groovy'

      if (env.INSTANCE_NAME == "AUTO"){
        job_name_acronym=""
        job_words=env.JOB_NAME.split("[-_ ]")
        for (i=0; i<job_words.size(); i++){
          job_name_acronym += job_words[i][0]
        }
        build_name = "${job_name_acronym}-${env.BUILD_NUMBER}"
      }
      else {
        build_name = env.INSTANCE_NAME
      }

      resources = allocate (
        name: build_name,
        count: 1,
        region: env.REGION,
        flavor: env.FLAVOR,
        image: env.IMAGE,
        keyname: env.KEYNAME,
      )
    } //stage
  ) //conditionalStages
  common.conditionalStage(
    stage_name: "Connect Slave",
    stage: {
      def connect_slave = load 'rpc-gating/pipeline-steps/connect_slave.groovy'
      connect_slave()
  })
  return resources
}
def delPubCloudSlave(Map args){
  common = load './rpc-gating/pipeline-steps/common.groovy'
  common.conditionalStep(
    step_name: "Pause",
    step: {
      input message: "Continue?"
    }
  )
  common.conditionalStep(
    step_name: 'Cleanup',
    step: {
      def remove_slave = load 'rpc-gating/pipeline-steps/remove_slave.groovy'
      remove_slave()
      def cleanup = load 'rpc-gating/pipeline-steps/cleanup_pubcloud.groovy'
      cleanup resources: args.resources
    } //stage
  ) //conditionalStage
}

return this
