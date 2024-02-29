import React from 'react';
import {LazyLog, ScrollFollow} from 'react-lazylog';


let api = '/api/container/';


export default class ContainerLog extends React.Component {

  state = {
    downloadFilePath: null
  }


  render() {
    const {hostId, containerId} = this.props;
    let url = api + "log/" + hostId + "/" + containerId;
    const downloadUrl = `api/container/downloadLog?hostId=${hostId}&containerId=${containerId}`
    return <div style={{height: 'calc(100vh - 350px)', minHeight: 400, width: '100%'}}>


      <div className='flex justify-end'>
        <a  href={downloadUrl} target="_blank">下载</a>
      </div>

      <ScrollFollow
        startFollowing={true}
        render={({follow, onScroll}) => {
          return (
            <LazyLog url={url}
                     fetchOptions={{credentials: 'include'}}
                     stream follow={follow} onScroll={onScroll}/>
          );
        }}
      />
    </div>
  }


}



