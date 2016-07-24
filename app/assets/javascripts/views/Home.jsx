"use strict";

//TODO: Build Layout
class Home extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return ( null );
  }
}

class View extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return ( 
      <div>
        <h1>{this.props.msg}</h1>
      </div>  
    );
  }
}