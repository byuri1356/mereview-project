import { Button, FloatLabelInput } from "../common/index";
import "../../styles/css/LoginForm.css";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import { useState } from "react";

const LoginForm = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const onChange = (event: any) => {
    if (event.target.id === "email") {
      setEmail(event.target.value);
    } else if (event.target.id === "password") {
      setPassword(event.target.value);
    }
  };
  const Login = (event: any) => {
    event.preventDefault();
    const userData = {
      email,
      password,
    };
    console.log(userData);
  };

  return (
    <Container>
      <Row>
        <form onSubmit={Login}>
          <Row>
            <FloatLabelInput
              id="email"
              placeholder="Email"
              onChange={onChange}
              value={email}
            />
            <FloatLabelInput
              id="password"
              placeholder="Password"
              onChange={onChange}
              value={password}
              type="password"
            />
          </Row>
          <Row className="justify-content-center">
            <a href="">비밀번호를 잊으셨나요?</a>
          </Row>

          <Row className="justify-content-end">
            <Button styles="btn-primary" text="LOGIN" btnType="submit"></Button>
          </Row>
        </form>
      </Row>
    </Container>
  );
};

export default LoginForm;
