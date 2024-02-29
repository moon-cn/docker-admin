import {LazyLog, ScrollFollow} from "react-lazylog";
import React from "react";
/**
 * https://mozilla-frontend-infra.github.io/react-lazylog/
 */
export default class extends React.Component {

  render() {
    const url = this.props.url;
    return <ScrollFollow
      startFollowing={true}
      render={({follow, onScroll}) => (
        <LazyLog url={url}
                 height={500}
                 fetchOptions={{credentials: 'include'}}
                 stream
                 follow={follow}
                 onScroll={onScroll}/>
      )}
    />

  }
}
