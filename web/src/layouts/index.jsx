import Admin from "./admin";

export default function (props){
  const {pathname} = props.location
  if(pathname == '/login'){
    return props.children;
  }

  return <Admin {...props} />

}
