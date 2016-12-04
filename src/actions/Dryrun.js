import * as schema from './Schema'
import {getReferential, postReferential} from '../utils/Action'

export const fetchDryruns = (exerciseId, noloading) => (dispatch) => {
  var uri = '/api/exercises/' + exerciseId + '/dryruns'
  return getReferential(schema.arrayOfDryruns, uri, noloading)(dispatch)
}

export const fetchDryrun = (exerciseId, dryrunId, noloading) => (dispatch) => {
  var uri = '/api/exercises/' + exerciseId + '/dryruns/' + dryrunId
  return getReferential(schema.dryrun, uri, noloading)(dispatch)
}

export const addDryrun = (exerciseId, data) => (dispatch) => {
  var uri = '/api/exercises/' + exerciseId + '/dryruns'
  return postReferential(schema.dryrun, uri, data)(dispatch)
}